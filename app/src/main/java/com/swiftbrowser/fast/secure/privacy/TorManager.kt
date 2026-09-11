/*
 * Swift Browser - A premium, private, and secure web browser.
 * Copyright (C) 2026 RebelRoot Ltd
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.swiftbrowser.fast.secure.privacy

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.IOException
import java.io.InputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketAddress

sealed class TorState {
    object Disconnected : TorState()
    object Connecting : TorState()
    data class Bootstrap(val percent: Int) : TorState()
    object Connected : TorState()
    data class Error(val message: String) : TorState()
}

class TorManager(private val context: Context) {

    companion object {
        private const val TAG = "TorManager"
        // Diagnostic-only tag shared across the whole proxy lifecycle (TorManager,
        // BrowserViewModel's currentProxyEndpoint/applyProxyPrefsLive, and the
        // onLoadError handler) so `adb logcat -s SwiftProxyDebug` gives one clean,
        // ordered timeline instead of hunting through each component's own tag.
        // Diagnostic-only — not gated behind isDebug because it mirrors the existing
        // TAG logging pattern in this file, but it should be trimmed back down once
        // the current proxy investigation is resolved (see task notes).
        const val PROXY_DEBUG_TAG = "SwiftProxyDebug"
        const val DEFAULT_SOCKS_PORT = 9050
        const val BRIDGE_SOCKS_PORT = 9052
        const val CONTROL_PORT = 9051

        const val ORBOT_PACKAGE = "org.torproject.android"
        const val ORBOT_START_ACTION = "org.torproject.android.intent.action.START"
        const val ORBOT_STOP_ACTION = "org.torproject.android.intent.action.STOP"

        /** Maximum time to wait for Orbot to become reachable after launch intent. */
        private const val ORBOT_WAIT_TIMEOUT_MS = 30_000L
        /** Polling interval while waiting for Orbot. */
        private const val ORBOT_POLL_INTERVAL_MS = 1_500L
    }

    private val _state = MutableStateFlow<TorState>(TorState.Disconnected)
    val state: StateFlow<TorState> = _state.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var testJob: Job? = null
    private var customSocksHost: String? = null
    private var customSocksPort: Int = DEFAULT_SOCKS_PORT
    private var customSocksUsername: String? = null
    private var customSocksPassword: String? = null

    fun setCustomProxy(host: String, port: Int, username: String? = null, password: String? = null) {
        customSocksHost = host
        customSocksPort = port
        customSocksUsername = username?.takeIf { it.isNotBlank() }
        customSocksPassword = password
    }

    fun clearCustomProxy() {
        customSocksHost = null
        customSocksPort = DEFAULT_SOCKS_PORT
        customSocksUsername = null
        customSocksPassword = null
    }

    fun startTor(port: Int = DEFAULT_SOCKS_PORT) {
        if (testJob?.isActive == true) {
            Log.d(PROXY_DEBUG_TAG, "startTor(): already have an active testJob, ignoring re-entrant call")
            return
        }

        Log.d(PROXY_DEBUG_TAG, "startTor(port=$port) called — customSocksHost=$customSocksHost customSocksPort=$customSocksPort hasCustomAuth=${!customSocksUsername.isNullOrEmpty()}")
        _state.value = TorState.Connecting
        testJob = scope.launch {
            try {
                val targetHost = customSocksHost ?: "127.0.0.1"
                val targetPort = customSocksHost?.let { customSocksPort } ?: port

                if (customSocksHost == null) {
                    Log.d(PROXY_DEBUG_TAG, "startTor(): no custom host configured -> Orbot path, target=$targetHost:$targetPort")
                    val orbotOk = tryStartOrbot()
                    Log.d(PROXY_DEBUG_TAG, "startTor(): tryStartOrbot() -> $orbotOk")
                    if (!orbotOk) {
                        _state.value = TorState.Error("Orbot not installed or failed to start. Install Orbot from F-Droid or Play Store, or configure a custom SOCKS5 proxy.")
                        return@launch
                    }
                    // Poll until Orbot's SOCKS port is actually reachable (up to 30s)
                    val reachable = pollUntilReachable(targetHost, targetPort)
                    Log.d(PROXY_DEBUG_TAG, "startTor(): pollUntilReachable($targetHost:$targetPort) -> $reachable")
                    if (!reachable) {
                        _state.value = TorState.Error("Orbot launched but SOCKS proxy not reachable after ${ORBOT_WAIT_TIMEOUT_MS / 1000}s. Open Orbot and ensure it shows 'Connected'.")
                        return@launch
                    }
                } else {
                    // Custom proxy: single test. Auth failures are surfaced distinctly
                    // from plain unreachability so the user knows to fix credentials
                    // rather than assume the host/port is wrong.
                    Log.d(PROXY_DEBUG_TAG, "startTor(): custom proxy path, target=$targetHost:$targetPort hasAuth=${!customSocksUsername.isNullOrEmpty()}")
                    try {
                        performSocksHandshake(targetHost, targetPort, customSocksUsername, customSocksPassword)
                        Log.d(PROXY_DEBUG_TAG, "startTor(): performSocksHandshake($targetHost:$targetPort) completed without throwing -> handshake OK")
                    } catch (e: SocksAuthException) {
                        Log.e(PROXY_DEBUG_TAG, "startTor(): SocksAuthException for $targetHost:$targetPort -> ${e.message}", e)
                        _state.value = TorState.Error(e.message ?: "SOCKS5 authentication failed")
                        return@launch
                    } catch (e: Exception) {
                        Log.e(PROXY_DEBUG_TAG, "startTor(): handshake FAILED for $targetHost:$targetPort -> ${e::class.simpleName}: ${e.message}", e)
                        _state.value = TorState.Error(
                            "Custom SOCKS5 proxy unreachable at $targetHost:$targetPort" +
                                (e.message?.let { " — $it" } ?: "")
                        )
                        return@launch
                    }
                }

                Log.i(PROXY_DEBUG_TAG, "startTor(): -> TorState.Connected (target=$targetHost:$targetPort)")
                _state.value = TorState.Connected
            } catch (e: Exception) {
                Log.e(TAG, "Tor connection failed", e)
                Log.e(PROXY_DEBUG_TAG, "startTor(): -> TorState.Error (unexpected): ${e::class.simpleName}: ${e.message}", e)
                _state.value = TorState.Error(e.message ?: "Unknown Tor error")
            } finally {
                testJob = null
            }
        }
    }

    fun stopTor() {
        testJob?.cancel()
        testJob = null
        _state.value = TorState.Disconnected
        // Ask Orbot to stop its Tor service so it doesn't linger in the
        // background. The stop action is a broadcast handled by Orbot's
        // StartTorReceiver; if Orbot isn't running this is a harmless no-op.
        try {
            val stopIntent = Intent(ORBOT_STOP_ACTION).apply {
                `package` = ORBOT_PACKAGE
            }
            context.sendBroadcast(stopIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send Orbot stop broadcast", e)
        }
    }

    fun isConnected(): Boolean {
        return _state.value is TorState.Connected
    }

    /** Cancels background coroutines. Call from ViewModel.onCleared(). */
    fun shutdown() {
        testJob?.cancel()
        testJob = null
        scope.cancel()
    }

    /**
     * Opens Orbot's UI so the user can manually tap "New Identity".
     *
     * Orbot does not expose a broadcast intent for NEWNYM, and the Tor
     * control port (9051) requires cookie authentication that we cannot
     * obtain without root or a shared UID. So the best we can do is
     * foreground Orbot and let the user trigger the circuit rotation
     * themselves. This is a no-op when using a custom/remote SOCKS proxy
     * (no control channel exists).
     */
    fun requestNewCircuit() {
        if (customSocksHost == null) {
            try {
                val pm = context.packageManager
                val intent = pm.getLaunchIntentForPackage(ORBOT_PACKAGE)
                if (intent != null) {
                    // TorManager is built with the application context, so any
                    // activity started from it MUST carry FLAG_ACTIVITY_NEW_TASK
                    // or Android throws AndroidRuntimeException (crash).
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to open Orbot for new circuit", e)
            }
        }
    }

    private fun tryStartOrbot(): Boolean {
        return try {
            val pm = context.packageManager
            // Use the launch intent as the install probe. getPackageInfo(pkg, 0)
            // is deprecated on API 33+ and throws NameNotFoundException on some
            // OEMs; a null launch intent reliably means Orbot is not installed
            // (or exposes no launcher activity).
            val launchIntent = pm.getLaunchIntentForPackage(ORBOT_PACKAGE)
            if (launchIntent == null) return false

            // Orbot's StartTorReceiver is a BroadcastReceiver, so the start
            // action must be sent as a broadcast — NOT via startActivity.
            val startIntent = Intent(ORBOT_START_ACTION).apply {
                `package` = ORBOT_PACKAGE
            }
            val resolved = pm.queryBroadcastReceivers(startIntent, 0)
            if (resolved.isNotEmpty()) {
                context.sendBroadcast(startIntent)
            } else {
                // Fallback: open Orbot's UI so the user can start it manually.
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start Orbot", e)
            false
        }
    }

    /**
     * Polls the SOCKS endpoint until it responds or the timeout elapses.
     * Emits Connecting state throughout — we do NOT synthesize a fake
     * bootstrap percentage from the poll counter. Real Tor bootstrap progress
     * would require reading Orbot's control port (9051), which is not
     * exposed by default. The UI shows an honest indeterminate "Waiting…"
     * state instead of an invented number.
     */
    private suspend fun pollUntilReachable(host: String, port: Int): Boolean {
        val deadline = System.currentTimeMillis() + ORBOT_WAIT_TIMEOUT_MS

        while (System.currentTimeMillis() < deadline) {
            _state.value = TorState.Connecting

            if (testSocksProxy(host, port)) {
                return true
            }
            delay(ORBOT_POLL_INTERVAL_MS)
        }
        return false
    }

    private fun testSocksProxy(host: String, port: Int): Boolean {
        return try {
            performSocksHandshake(host, port, null, null)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Thrown specifically when the SOCKS5 server completes the username/password
     * sub-negotiation (RFC 1929) but rejects the credentials, or demands auth we
     * have none for — so callers can tell "wrong password" apart from "host down".
     */
    private class SocksAuthException(message: String) : IOException(message)

    /**
     * Performs a full SOCKS5 handshake against [host]:[port], including the
     * RFC 1929 username/password sub-negotiation when [username] is non-null,
     * then probes with a minimal CONNECT so a proxy that accepts the auth
     * handshake but silently drops connections is still caught. Throws on any
     * failure; callers that just want a yes/no should catch and discard.
     */
    @Throws(IOException::class)
    private fun performSocksHandshake(host: String, port: Int, username: String?, password: String?) {
        Socket().use { socket ->
            Log.d(PROXY_DEBUG_TAG, "performSocksHandshake: connecting to $host:$port (connect timeout 2500ms)")
            socket.connect(InetSocketAddress(host, port), 2500)
            socket.soTimeout = 2500
            Log.d(PROXY_DEBUG_TAG, "performSocksHandshake: TCP connected to $host:$port, soTimeout=2500ms")
            val out = socket.getOutputStream()
            val inp = socket.getInputStream()

            val hasCreds = !username.isNullOrEmpty()
            // Offer no-auth (0x00) and, when we have credentials, username/password
            // (0x02) so a server that requires auth can select it.
            val methods = if (hasCreds) byteArrayOf(0x00, 0x02) else byteArrayOf(0x00)
            val greeting = byteArrayOf(0x05, methods.size.toByte()) + methods
            Log.d(PROXY_DEBUG_TAG, "performSocksHandshake: sending greeting ${greeting.toHexLog()} (hasCreds=$hasCreds)")
            out.write(greeting)
            out.flush()

            val header = readFully(inp, 2)
            Log.d(PROXY_DEBUG_TAG, "performSocksHandshake: greeting reply ${header.toHexLog()}")
            val ver = header[0].toInt() and 0xFF
            val method = header[1].toInt() and 0xFF
            if (ver != 5) throw IOException("Not a SOCKS5 proxy (version=$ver)")
            if (method == 0xFF) throw IOException("SOCKS5 proxy rejected all authentication methods")
            Log.d(PROXY_DEBUG_TAG, "performSocksHandshake: server selected auth method=0x${method.toString(16).padStart(2, '0')}")

            if (method == 0x02) {
                if (!hasCreds) throw IOException("SOCKS5 proxy requires a username and password")
                val uBytes = username!!.toByteArray(Charsets.UTF_8)
                val pBytes = (password ?: "").toByteArray(Charsets.UTF_8)
                val authReq = byteArrayOf(0x01, uBytes.size.toByte()) + uBytes +
                    byteArrayOf(pBytes.size.toByte()) + pBytes
                // Never log uBytes/pBytes themselves — only lengths — so credentials
                // never land in logcat even in this diagnostic build.
                Log.d(PROXY_DEBUG_TAG, "performSocksHandshake: sending RFC1929 auth (ulen=${uBytes.size}, plen=${pBytes.size})")
                out.write(authReq)
                out.flush()
                val authResp = readFully(inp, 2)
                Log.d(PROXY_DEBUG_TAG, "performSocksHandshake: auth reply ${authResp.toHexLog()}")
                if ((authResp[1].toInt() and 0xFF) != 0x00) {
                    throw SocksAuthException("SOCKS5 authentication failed — check username/password")
                }
            }

            // ATYP=0x01 (IPv4) requires EXACTLY 4 address octets per RFC 1928 —
            // this was previously 5 zero bytes, sending a malformed 11-byte
            // CONNECT request (1 stray trailing byte) instead of the correct
            // 10-byte one. Every octet here happens to be zero, so lenient
            // servers still parse DST.ADDR/DST.PORT as 0.0.0.0:0 correctly by
            // coincidence, but the extra unconsumed byte then sits in the
            // stream where a stricter/minimal proxy implementation (typical of
            // random public SOCKS5 proxies) reads it as the start of malformed
            // trailing data and aborts the connection with a TCP reset — which
            // surfaced to users as a misleading "unreachable ... connection
            // reset" error even though the proxy was live and reachable.
            val addr = byteArrayOf(0, 0, 0, 0)
            val portBytes = byteArrayOf(0, 0)
            val req = byteArrayOf(0x05, 0x01, 0x00, 0x01) + addr + portBytes
            Log.d(PROXY_DEBUG_TAG, "performSocksHandshake: sending probe CONNECT ${req.toHexLog()} (${req.size} bytes, target=0.0.0.0:0)")
            out.write(req)
            out.flush()

            val resp = readFully(inp, 10)
            Log.d(PROXY_DEBUG_TAG, "performSocksHandshake: CONNECT reply ${resp.toHexLog()} (ver=${resp[0].toInt() and 0xFF}, rep=0x${(resp[1].toInt() and 0xFF).toString(16).padStart(2, '0')})")
            val respVer = resp[0].toInt() and 0xFF
            // The probe target (0.0.0.0:0) is intentionally unroutable, so any
            // real, correctly implemented proxy is expected to REFUSE it —
            // typically REP=0x05 (connection refused) or 0x04 (host
            // unreachable), never 0x00 (succeeded). Requiring REP==0x00 here
            // would reject every genuinely working proxy along with actually
            // broken ones, which is what was happening. Receiving ANY
            // well-formed SOCKS5 reply frame (correct version byte, full 10
            // bytes) already proves the proxy is alive, speaks the protocol,
            // and — if auth was required — already accepted our credentials
            // to get this far; that's everything this connectivity check
            // needs to confirm. Only a connection that never produces a
            // well-formed reply at all (timeout, reset, garbage, EOF) should
            // read as "broken/unreachable", and those already throw before
            // reaching this line.
            if (respVer != 5) throw IOException("SOCKS5 request failed: malformed reply (version=$respVer)")
        }
    }

    private fun readFully(inp: InputStream, n: Int): ByteArray {
        val buf = ByteArray(n)
        var read = 0
        while (read < n) {
            val r = inp.read(buf, read, n - read)
            if (r == -1) throw IOException("SOCKS proxy closed connection")
            read += r
        }
        return buf
    }

    /** Diagnostic-only hex dump for SwiftProxyDebug wire-level logging. */
    private fun ByteArray.toHexLog(): String = joinToString(" ") { "%02x".format(it) }
}

# Third-Party Licenses

## omni-browser

Swift Browser's GeckoView-based browsing engine, extension system, and a
substantial portion of its application code are built on source from the
**omni-browser** project:

- Project: omni-browser
- Repository: https://github.com/REBEL-ROOT/omni-browser
- Copyright: (C) 2026 RebelRoot Ltd
- License: GNU General Public License v3.0 (GPLv3)

The full text of the GPLv3 license under which that source is provided is
included in this repository at [`LICENSE`](LICENSE), and is also available
at <https://www.gnu.org/licenses/gpl-3.0.html>.

Per GPLv3 §5 and §7(b), the original copyright notices in files derived
from omni-browser have been preserved in place (see the header comment at
the top of each such source file), and this repository's own `LICENSE`
file carries the same GPLv3 terms forward for the combined work.

This notice, and the corresponding one-line attribution shown in
Settings → About in the app itself, are the required legal attribution
for that upstream project — not a statement of affiliation with or
endorsement by RebelRoot Ltd.

## Other third-party components

Swift Browser also depends on a number of other open-source libraries and
components (e.g. Mozilla GeckoView, AndroidX/Jetpack libraries, jlibtorrent,
WireGuard, SQLCipher, Vosk) via standard Gradle dependencies declared in
`app/build.gradle.kts`. Each is used under its own respective license;
this file does not attempt to enumerate every transitive dependency's
license text — see each dependency's own project page for details.

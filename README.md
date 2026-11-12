<<<<<<< HEAD
# TatvaOps — Selenium Automation (Java + TestNG, Page Object Model)

A Maven Selenium framework structured for the TatvaOps SPA, ready to open in
**Antigravity** (or Cursor / IntelliJ / VS Code — all VS Code-compatible tooling works).

## Structure

```
src/main/java
  pages/       BasePage, LoginPage, HomePage      (locators + actions)
  utilities/   BrowserFactory, ConfigReader,
               ExcelUtility, ScreenshotUtility
  base/        BaseTest                            (driver lifecycle + screenshot-on-fail)
src/test/java
  tests/       LoginTest, DashboardTest, SmokeTest
  runners/     TestRunner                          (optional programmatic runner)
src/test/resources
  config.properties, testdata.xlsx, testng.xml, logback.xml
screenshots/  reports/  logs/                      (output dirs)
pom.xml
```

## Prerequisites

- JDK 17+  (set JAVA_HOME)
- Maven 3.9+
- Chrome/Firefox/Edge installed. Selenium 4.6+ auto-resolves the driver binary
  via Selenium Manager — no manual chromedriver download needed.

## Setup

1. Open the folder in Antigravity.
2. Ensure the Java + Maven extensions are active (Antigravity is VS Code-based;
   install the "Extension Pack for Java" if not already present).
3. Edit `src/test/resources/config.properties`:
   - set `validUsername` / `validPassword` to a **test/staging** account
   - adjust `browser` / `headless` as needed
4. Update the placeholder locators in `pages/*.java` to match the real DOM.
   Prefer `data-testid` selectors (the ones you're requesting from the devs) —
   they survive SPA re-renders far better than hashed CSS classes.

## Running

```bash
mvn clean test                 # runs the full testng.xml suite
mvn test -Dgroups=smoke        # smoke only
```

Or run `testng.xml` / individual test classes directly from the IDE.

## Connecting MCP servers in Antigravity

MCP is for **agent-driven exploration and test authoring** — not your committed
suite. Use it to click through the app, discover locators, and reproduce bugs;
keep the real regression suite as the code above.

1. Open Antigravity settings → MCP / connectors section.
2. Add the servers from `mcp_config.json` (merge into Antigravity's own MCP
   settings file; the exact path is under its MCP settings UI):

```json
{
  "mcpServers": {
    "selenium":   { "command": "npx", "args": ["-y", "@angiejones/mcp-selenium"] },
    "playwright": { "command": "npx", "args": ["-y", "@playwright/mcp@latest"] }
  }
}
```

3. Reload Antigravity. The browser tools become available to the agent.
   You can then ask it to open the app, inspect elements, and suggest selectors
   you paste back into the page objects.

> Requires Node.js installed (for `npx`). The Playwright server is included
> because it's the more robust option for this auth-gated SPA; use whichever
> you prefer for exploration.

## Notes / TODO

- Locators in `pages/` are placeholders — replace before running for real.
- `testdata.xlsx` ships with edge-case invalid-login rows; extend as needed.
- Add ExtentReports wiring in a TestNG listener when you want richer HTML reports
  (dependency is already in pom.xml).
=======
# SeleniumAutomation
>>>>>>> 97f680ba18af1121a71542a90ff5d66ae196a9b3

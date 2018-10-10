name := "corbeans"
version := "1.0"
scalaVersion := "2.11.8"

lazy val noPublishSettings = Seq(
  publish := ((): Unit),
  publishLocal := ((): Unit),
  publishArtifact := false
)

lazy val micrositeSettings = Seq(
  micrositeName := "corbeans",
  micrositeDescription := "Corbeans Documentation",
  micrositeBaseUrl := "corbeans",
  micrositeDocumentationUrl := "/corbeans/docs/",
  micrositeGithubOwner := "manosbatsis",
  micrositeGithubRepo := "corbeans",
  micrositeGitterChannel := false,
  micrositeHighlightTheme := "github",
  micrositeGithubToken := Option(System.getenv().get("GITHUB_TOKEN")),
  micrositePushSiteWith := GitHub4s,
  micrositeGithubToken := getEnvVar("GH_REPO_ACCESS_TOKEN")
)


lazy val docs = (project in file("docs"))
  .settings(moduleName := "docs")
  .settings(micrositeSettings: _*)
  .settings(noPublishSettings: _*)
  .enablePlugins(MicrositesPlugin)
  .enablePlugins(TutPlugin)
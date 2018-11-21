name := "corbeans"
organizationName := "Manos Batsis"
version := "0.17"
scalaVersion := "2.11.8"

lazy val noPublishSettings = Seq(
  publish := ((): Unit),
  publishLocal := ((): Unit),
  publishArtifact := false
)

lazy val micrositeSettings = Seq(
  micrositeName := "corbeans",
  micrositeDescription := "Corda integration for Spring-Boot",
  micrositeUrl := "https://manosbatsis.github.io",
  micrositeBaseUrl := "/corbeans",
  micrositeDocumentationUrl := "/corbeans/docs",
  micrositeAuthor := "Manos Batsis",
  micrositeHomepage := "https://manosbatsis.github.io/corbeans/",
  micrositeOrganizationHomepage := "https://manosbatsis.github.io",
  micrositeGithubOwner := "manosbatsis",
  micrositeGithubRepo := "corbeans",
  micrositeGithubToken := sys.env.get("GITHUB_TOKEN"),
  micrositePushSiteWith := GitHub4s,
  micrositeGithubLinks := true,
  micrositeGitterChannel := true,
  micrositeHighlightTheme := "github",
  micrositeHighlightLanguages := Seq("kotlin", "java", "gradle", "xml", "bash", "properties"),
  micrositeStaticDirectory := file("build/dokka"),
  micrositeFooterText := Some("we are in accord"),
  micrositeShareOnSocial := false,
)


lazy val docs = (project in file("docs"))
  .settings(moduleName := "docs")
  .settings(micrositeSettings: _*)
  .settings(noPublishSettings: _*)
  .enablePlugins(MicrositesPlugin)
 // .enablePlugins(GitHub4s)
  .enablePlugins(TutPlugin)
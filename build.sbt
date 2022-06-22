import scoverage.ScoverageKeys
import uk.gov.hmrc.DefaultBuildSettings.integrationTestSettings
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings

val appName = "helpline-frontend"

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin)
  .settings(PlayKeys.playDefaultPort := 10102)
  .settings(
    majorVersion                     := 1,
    scalaVersion                     := "2.12.15",
    libraryDependencies              ++= AppDependencies.compile ++ AppDependencies.test,
    TwirlKeys.templateImports ++= Seq(
      "uk.gov.hmrc.helplinefrontend.config.AppConfig",
      "uk.gov.hmrc.govukfrontend.views.html.components._",
      "uk.gov.hmrc.hmrcfrontend.views.html.components._",
      "uk.gov.hmrc.helplinefrontend.models.ui._"
    ),
  )
  .settings(publishingSettings: _*)
  .configs(IntegrationTest)
  .settings(integrationTestSettings(): _*)
  .settings(resolvers += Resolver.jcenterRepo)
  .settings(
    Seq(
      ScoverageKeys.coverageExcludedPackages :=
        """<empty>;
          |Reverse.*;
          |.*BuildInfo.*;
          |.*Routes.*;
          |.*RoutesPrefix.*;""".stripMargin,
      ScoverageKeys.coverageMinimumStmtTotal := 80,
      ScoverageKeys.coverageFailOnMinimum := false,
      ScoverageKeys.coverageHighlighting := true
    )
  )
  .settings(A11yTest / unmanagedSourceDirectories += (baseDirectory.value / "accessibility"))
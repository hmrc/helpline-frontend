import scoverage.ScoverageKeys
import uk.gov.hmrc.DefaultBuildSettings

val appName = "helpline-frontend"

ThisBuild / majorVersion := 1
ThisBuild / scalaVersion := "3.3.6"

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin)
  .settings(
    PlayKeys.playDefaultPort := 10102,
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
    scalacOptions ++= Seq(
      "-Wconf:msg=Flag.*repeatedly:s",
      "-Wconf:src=routes/.*&msg=unused import:silent",
      "-Wconf:src=routes/.*&msg=unused private member:silent",
      "-Wconf:src=html/.*&msg=unused import:silent",
      "-Wconf:src=twirl/.*&msg=unused import:silent",
      "-Xfatal-warnings",
      "-deprecation"
    ),
    TwirlKeys.templateImports ++= Seq(
      "uk.gov.hmrc.helplinefrontend.config.AppConfig",
      "uk.gov.hmrc.govukfrontend.views.html.components._",
      "uk.gov.hmrc.hmrcfrontend.views.html.components._",
      "uk.gov.hmrc.helplinefrontend.models.ui._"
    )
  )
  .settings(
    Seq(
      ScoverageKeys.coverageExcludedPackages := "<empty>;Reverse.*;.*BuildInfo.*;.*Routes.*;.*RoutesPrefix.*;",
      ScoverageKeys.coverageMinimumStmtTotal := 78,
      ScoverageKeys.coverageFailOnMinimum := true,
      ScoverageKeys.coverageHighlighting := true
    )
  )
  .settings(A11yTest / unmanagedSourceDirectories += (baseDirectory.value / "accessibility"))

lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test")
  .settings(
    DefaultBuildSettings.itSettings(),
    scalacOptions ++= Seq("-Wconf:msg=Flag.*repeatedly:s")
  )
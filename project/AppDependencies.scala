import play.core.PlayVersion
import sbt._

object AppDependencies {

  val compile = Seq(
    "uk.gov.hmrc"           %% "bootstrap-frontend-play-28" % "5.24.0",
    "uk.gov.hmrc"           %% "play-frontend-hmrc"         % "3.21.0-play-28",
    "uk.gov.hmrc"           %% "play-language"              % "5.3.0-play-28",
    "com.beachape"          %% "enumeratum"                 % "1.7.0"
  )

  val test = Seq(
    "com.typesafe.play"       %% "play-test"                % PlayVersion.current  % Test,
    "uk.gov.hmrc"             %% "bootstrap-test-play-28"   % "5.3.0"              % Test,
    "uk.gov.hmrc"             %% "service-integration-test" % "1.3.0-play-28"      % IntegrationTest,
    "org.jsoup"               %  "jsoup"                    % "1.13.1"             % Test,
    "org.scalamock"           %% "scalamock"                % "5.1.0"              % Test,
    "org.mockito"             % "mockito-core"              % "3.8.0"              % Test,
    "org.scalatest"           %% "scalatest"                % "3.2.5"              % "test, it",
    "com.vladsch.flexmark"    %  "flexmark-all"             % "0.36.8"             % "test, it",
    "org.scalatestplus.play"  %% "scalatestplus-play"       % "4.0.3"              % "test, it"
  )
}

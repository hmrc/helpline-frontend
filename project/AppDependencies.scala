import play.core.PlayVersion
import sbt._

object AppDependencies {

  val bootstrapPlayVerson = "7.23.0"

  val compile = Seq(
    "uk.gov.hmrc"           %% "bootstrap-frontend-play-28" % bootstrapPlayVerson,
    "uk.gov.hmrc"           %% "play-frontend-hmrc"         % "7.29.0-play-28",
    "com.beachape"          %% "enumeratum"                 % "1.7.3"
  )

  val test = Seq(
    "com.typesafe.play"       %% "play-test"                % PlayVersion.current  % Test,
    "uk.gov.hmrc"             %% "bootstrap-test-play-28"   % bootstrapPlayVerson  % Test,
    "org.jsoup"               %  "jsoup"                    % "1.17.2"             % Test,
    "org.scalamock"           %% "scalamock"                % "5.2.0"              % Test,
    "org.mockito"             % "mockito-core"              % "3.12.4"             % Test,
    "org.scalatest"           %% "scalatest"                % "3.2.9"              % "test, it",
    "com.vladsch.flexmark"    %  "flexmark-all"             % "0.36.8"             % "test, it",
    "org.scalatestplus.play"  %% "scalatestplus-play"       % "5.1.0"              % "test, it"
  )
}

import play.core.PlayVersion
import sbt._

object AppDependencies {

  val bootstrapPlayVerson = "9.6.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"           %% "bootstrap-frontend-play-30" % bootstrapPlayVerson,
    "uk.gov.hmrc"           %% "play-frontend-hmrc-play-30" % "11.8.0",
    "com.beachape"          %% "enumeratum"                 % "1.7.5",
    "commons-codec"         % "commons-codec"               % "1.17.1"

  )

  val test: Seq[ModuleID] = Seq(
    "org.playframework"       %% "play-test"                % PlayVersion.current  % Test,
    "uk.gov.hmrc"             %% "bootstrap-test-play-30"   % bootstrapPlayVerson  % Test,
    "org.jsoup"               %  "jsoup"                    % "1.18.3"             % Test,
    "org.scalamock"           %% "scalamock"                % "6.0.0"              % Test,
    "org.mockito"             %  "mockito-core"             % "5.14.2"             % Test,
    "org.scalatest"           %% "scalatest"                % "3.2.19"             % Test,
    "com.vladsch.flexmark"    %  "flexmark-all"             % "0.64.8"             % Test,
    "org.scalatestplus.play"  %% "scalatestplus-play"       % "7.0.1"              % Test
  )
}

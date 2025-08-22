import play.core.PlayVersion
import sbt.*

object AppDependencies {

  val bootstrapPlayVersion = "10.1.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"           %% "bootstrap-frontend-play-30" % bootstrapPlayVersion,
    "uk.gov.hmrc"           %% "play-frontend-hmrc-play-30" % "12.8.0",
    "com.beachape"          %% "enumeratum"                 % "1.9.0",
    "commons-codec"         % "commons-codec"               % "1.19.0"

  )

  val test: Seq[ModuleID] = Seq(
    "org.playframework"       %% "play-test"                % PlayVersion.current  % Test,
    "uk.gov.hmrc"             %% "bootstrap-test-play-30"   % bootstrapPlayVersion  % Test,
    "org.jsoup"               %  "jsoup"                    % "1.21.1"             % Test,
    "org.scalamock"           %% "scalamock"                % "7.4.1"              % Test,
    "org.mockito"             %  "mockito-core"             % "5.19.0"             % Test,
    "org.scalatest"           %% "scalatest"                % "3.2.19"             % Test,
    "com.vladsch.flexmark"    %  "flexmark-all"             % "0.64.8"             % Test,
    "org.scalatestplus.play"  %% "scalatestplus-play"       % "7.0.2"              % Test
  )
}

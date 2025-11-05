import sbt.*

object AppDependencies {

  val bootstrapPlayVersion = "10.4.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"           %% "bootstrap-frontend-play-30" % bootstrapPlayVersion,
    "uk.gov.hmrc"           %% "play-frontend-hmrc-play-30" % "12.20.0",
    "com.beachape"          %% "enumeratum"                 % "1.9.0"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-30"   % bootstrapPlayVersion,
    "org.scalatestplus"       %% "mockito-4-11"             % "3.2.18.0",
    "org.scalamock"           %% "scalamock"                % "7.5.1",
    "org.mockito"             %  "mockito-core"             % "5.20.0",
    "org.scalatest"           %% "scalatest"                % "3.2.19"
  ).map(_ % "test")
}

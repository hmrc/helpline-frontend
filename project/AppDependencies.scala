import sbt.*

object AppDependencies {

  val bootstrapPlayVersion = "10.5.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"           %% "bootstrap-frontend-play-30" % bootstrapPlayVersion,
    "uk.gov.hmrc"           %% "play-frontend-hmrc-play-30" % "12.28.0",
    "com.beachape"          %% "enumeratum"                 % "1.9.2"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-30"   % bootstrapPlayVersion,
    "org.scalatestplus"       %% "mockito-4-11"             % "3.2.18.0",
    "org.scalamock"           %% "scalamock"                % "7.5.3",
    "org.mockito"             %  "mockito-core"             % "5.21.0",
    "org.scalatest"           %% "scalatest"                % "3.2.19"
  ).map(_ % "test")
}

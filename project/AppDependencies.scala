import sbt.*

object AppDependencies {

  private val bootstrapPlayVersion = "10.5.0"
  private val playSuffix = "-play-30"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% s"bootstrap-frontend$playSuffix" % bootstrapPlayVersion,
    "uk.gov.hmrc" %% s"play-frontend-hmrc$playSuffix" % "12.27.0"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"       %% s"bootstrap-test$playSuffix"   % bootstrapPlayVersion,
    "uk.gov.hmrc.mongo" %% s"hmrc-mongo-test$playSuffix"  % "2.11.0"
  ).map(_ % Test)
}

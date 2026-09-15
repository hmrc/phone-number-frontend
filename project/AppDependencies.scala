import sbt.*

object AppDependencies {

  private val bootstrapPlayVersion = "10.8.0"
  private val hmrcPlayFrontendVersion = "13.13.0"
  private val hmrcMongoVersion = "2.13.0"
  private val playSuffix = "-play-30"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% s"bootstrap-frontend$playSuffix" % bootstrapPlayVersion,
    "uk.gov.hmrc" %% s"play-frontend-hmrc$playSuffix" % hmrcPlayFrontendVersion
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"       %% s"bootstrap-test$playSuffix"   % bootstrapPlayVersion,
    "uk.gov.hmrc.mongo" %% s"hmrc-mongo-test$playSuffix"  % hmrcMongoVersion
  ).map(_ % Test)
}

import play.sbt.PlayImport.*
import sbt.*

object AppDependencies {
  val hmrcBootstrapVersion = "7.19.0"

  val compile = Seq(
    "uk.gov.hmrc"             %% "bootstrap-frontend-play-28" % hmrcBootstrapVersion,
    "uk.gov.hmrc"             %% "play-frontend-hmrc"         % "7.14.0-play-28"
  )

  val test = Seq(
    "uk.gov.hmrc"            %% "bootstrap-test-play-28"   % hmrcBootstrapVersion % "test, it",
    "uk.gov.hmrc.mongo"      %% "hmrc-mongo-test-play-28"  % "1.3.0"             % IntegrationTest,
    "org.mockito"            %% "mockito-scala"            % "1.17.12"             % Test
  )
}

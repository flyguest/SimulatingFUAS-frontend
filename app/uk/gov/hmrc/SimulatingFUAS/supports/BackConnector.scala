package uk.gov.hmrc.SimulatingFUAS.supports

import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.WSResponse
import play.api.mvc.{AnyContent, Request}
import uk.gov.hmrc.SimulatingFUAS.config.WSHttp
import uk.gov.hmrc.SimulatingFUAS.controllers.NewEnvelopesController.inputEnvelopesBody
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.Future
import scala.util.Try

object BackConnector extends ServicesConfig with ActionsSupport {

  lazy val Url: String = baseUrl("file-backend")
  val http = WSHttp

  def createAnEmptyEnvelope()(implicit hc: HeaderCarrier, request: Request[AnyContent]): Object = {
    val submitInput = inputEnvelopesBody.bindFromRequest()
    val result = Try{Json.parse(submitInput.data("body"))}
    if (result.isFailure) result.failed.get
    else {
      client
        .url(s"$Url/file-upload/envelopes")
        .post(Json.parse(submitInput.data("body")))
    }
  }

  def deleteAnEnvelope(eid: String)(implicit hc: HeaderCarrier, request: Request[AnyContent]): Future[HttpResponse] = {
    http.DELETE(s"$Url/file-upload/envelopes/$eid")
  }

  def routeAnEnvelope(eid: String)(implicit hc: HeaderCarrier, request: Request[AnyContent]) = {
    val submitInput = inputEnvelopesBody.bindFromRequest()
    val result = Try{Json.parse(submitInput.data("body"))}
    if (result.isFailure) result.failed.get
    else http.POST(s"$Url/file-routing/requests", Json.parse(submitInput.data("body")))
  }

  def loadEnvelopeInf(eid: String)(implicit hc: HeaderCarrier): Future[JsValue] = {
    http.GET(s"$Url/file-upload/envelopes/$eid").map(response => response.json)
  }

  def loadEnvelopeEve(eid: String)(implicit hc: HeaderCarrier): Future[JsValue] = {
    http.GET(s"$Url/file-upload/events/$eid").map(response => response.json)
  }

  def loadInProgressFiles(implicit hc: HeaderCarrier): Future[JsValue] = {
    http.GET(s"$Url/file-upload/files/inprogress").map(response => response.json)
  }

  def deleteInProgressFile(fileRef: String)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    http.DELETE(s"$Url/file-upload/files/inprogress/$fileRef")
  }

  def downloadFile(eid: String, encodedFileId: String)(implicit hc: HeaderCarrier, request: Request[AnyContent]): Future[WSResponse] = {
    client
      .url(s"$Url/file-upload/envelopes/$eid/files/$encodedFileId/content")
      .get()
  }

  def deleteFile(eid: String, encodedFileId: String)(implicit hc: HeaderCarrier): Future[WSResponse] = {
    client
      .url(s"$Url/file-upload/envelopes/$eid/files/$encodedFileId")
      .delete()
  }

  def downloadEnvelope(eid: String)(implicit hc: HeaderCarrier, request: Request[AnyContent]): Future[WSResponse] = {
    client
      .url(s"$Url/file-transfer/envelopes/$eid")
      .get()
  }

  def deleteRoutedEnvelope(eid: String)(implicit hc: HeaderCarrier, request: Request[AnyContent]): Future[HttpResponse] = {
    http.DELETE(s"$Url/file-transfer/envelopes/$eid")
  }
}
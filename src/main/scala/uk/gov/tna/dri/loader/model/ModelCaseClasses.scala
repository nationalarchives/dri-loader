package uk.gov.tna.dri.loader.model

object ModelCaseClasses {
  case class SIPDowloadAvailableParams(`xip-zip-file-location`:String)
  case class SIPDownloadAvailable( `sip-download-available`:SIPDowloadAvailableParams)
  case class DRISIPDownloadedMessage (parameters:SIPDownloadAvailable)

  case class TransferMetadata(series:String, batch:String, transferringBody:String,
                              closureSchema:String, metadataSchema:String)

}

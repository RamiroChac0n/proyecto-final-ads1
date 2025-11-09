<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<dte:GTDocumento xmlns:dte="http://www.sat.gob.gt/dte/fel/0.2.0"
                 xmlns:caitem="http://www.sat.gob.gt/face2/ComplementoAlItem/0.1.0"
                 xmlns:cca="http://www.sat.gob.gt/face2/CobroXCuentaAjena/0.1.0"
                 xmlns:cesp="http://www.sat.gob.gt/face2/ComplementoEspectaculos/0.1.0"
                 xmlns:cex="http://www.sat.gob.gt/face2/ComplementoExportaciones/0.1.0"
                 xmlns:cexprov="http://www.sat.gob.gt/face2/ComplementoExportacionProvisional/0.1.0"
                 xmlns:cfc="http://www.sat.gob.gt/dte/fel/CompCambiaria/0.1.0"
                 xmlns:cfe="http://www.sat.gob.gt/face2/ComplementoFacturaEspecial/0.1.0"
                 xmlns:clepp="http://www.sat.gob.gt/face2/ComplementoPartidosPolitico/0.1.0"
                 xmlns:cmdp="http://www.sat.gob.gt/face2/ComplementoMediosDePago/0.1.0"
                 xmlns:cno="http://www.sat.gob.gt/face2/ComplementoReferenciaNota/0.1.0"
                 xmlns:crc="http://www.sat.gob.gt/face2/ComplementoReferenciaConstancia/0.1.0"
                 xmlns:crfe="http://www.sat.gob.gt/face2/ComplementoRetencionesFacturaEspecifica/0.1.0"
                 xmlns:ctrasmer="http://www.sat.gob.gt/face2/TrasladoMercancias/0.1.0"
                 xmlns:ctup="http://www.sat.gob.gt/face2/ComplementoTurismoPasaje/0.1.0"
                 xmlns:ds="http://www.w3.org/2000/09/xmldsig#"
                 xmlns:n1="http://www.altova.com/samplexml/other-namespace"
                 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                 Version="0.1"
                 xsi:schemaLocation="http://www.sat.gob.gt/dte/fel/0.2.0 GT_Documento-0.2.0.xsd">

    <dte:SAT ClaseDocumento="dte">
        <dte:DTE ID="DatosCertificados">
            <dte:DatosEmision ID="DatosEmision">
                <dte:DatosGenerales CodigoMoneda="GTQ"
                                   Dispositivo="APP"
                                   FechaHoraEmision="${fechaEmision}"
                                   Tipo="FPEQ" />

                <dte:Emisor AfiliacionIVA="PEQ"
                           CodigoEstablecimiento="${codigoEstablecimiento}"
                           CorreoEmisor="${correoEmisor}"
                           NITEmisor="${nitEmisor}"
                           NombreComercial="${nombreComercial}"
                           NombreEmisor="${nombreEmisor}">
                    <dte:DireccionEmisor>
                        <dte:Direccion>${direccionEmisor}</dte:Direccion>
                        <dte:CodigoPostal>${codigoPostal}</dte:CodigoPostal>
                        <dte:Municipio>${municipio}</dte:Municipio>
                        <dte:Departamento>${departamento}</dte:Departamento>
                        <dte:Pais>GT</dte:Pais>
                    </dte:DireccionEmisor>
                </dte:Emisor>

                <dte:Receptor CorreoReceptor="${correoReceptor}"
                             IDReceptor="${idReceptor}"
                             NombreReceptor="${nombreReceptor}" />

                <dte:Frases>
                    <dte:Frase CodigoEscenario="1" TipoFrase="3" />
                </dte:Frases>

                <dte:Items>
<#list items as item>
                    <dte:Item BienOServicio="B" NumeroLinea="${item.numeroLinea}">
                        <dte:Cantidad>${item.cantidad}</dte:Cantidad>
                        <dte:Descripcion>${item.descripcion}</dte:Descripcion>
                        <dte:PrecioUnitario>${item.precioUnitario}</dte:PrecioUnitario>
                        <dte:Precio>${item.precio}</dte:Precio>
                        <dte:Descuento>${item.descuento}</dte:Descuento>
                        <dte:Total>${item.total}</dte:Total>
                    </dte:Item>
</#list>
                </dte:Items>

                <dte:Totales>
                    <dte:GranTotal>${granTotal}</dte:GranTotal>
                </dte:Totales>
            </dte:DatosEmision>

            <dte:Certificacion>
                <dte:NITCertificador>16693949</dte:NITCertificador>
                <dte:NombreCertificador>Superintendencia de Administracion Tributaria</dte:NombreCertificador>
                <dte:NumeroAutorizacion Numero="${numeroAutorizacion}" Serie="${serieAutorizacion}">
                    ${uuidAutorizacion}
                </dte:NumeroAutorizacion>
                <dte:FechaHoraCertificacion>${fechaCertificacion}</dte:FechaHoraCertificacion>
            </dte:Certificacion>
        </dte:DTE>
    </dte:SAT>

    <ds:Signature Id="xmldsig-533cf373-f301-4cf2-8637-be9c008ffebd">
        <ds:SignedInfo>
            <ds:CanonicalizationMethod Algorithm="http://www.w3.org/TR/2001/REC-xml-c14n-20010315" />
            <ds:SignatureMethod Algorithm="http://www.w3.org/2001/04/xmldsig-more#rsa-sha256" />
            <ds:Reference Id="xmldsig-533cf373-f301-4cf2-8637-be9c008ffebd-ref0" URI="#DatosEmision">
                <ds:Transforms>
                    <ds:Transform Algorithm="http://www.w3.org/2000/09/xmldsig#enveloped-signature" />
                </ds:Transforms>
                <ds:DigestMethod Algorithm="http://www.w3.org/2001/04/xmlenc#sha256" />
                <ds:DigestValue>liJqYp97J/3yA5SbTRiI8gXtARlNToJGdbVOhYbnTNc=</ds:DigestValue>
            </ds:Reference>
            <ds:Reference Type="http://uri.etsi.org/01903#SignedProperties" URI="#xmldsig-533cf373-f301-4cf2-8637-be9c008ffebd-signedprops">
                <ds:DigestMethod Algorithm="http://www.w3.org/2001/04/xmlenc#sha256" />
                <ds:DigestValue>dRKUdUTRuom4rV3MBo5hhBWFDCEdE4V6cU68IRRDaNw=</ds:DigestValue>
            </ds:Reference>
        </ds:SignedInfo>
        <ds:SignatureValue Id="xmldsig-533cf373-f301-4cf2-8637-be9c008ffebd-sigvalue">
sgG3CrE8OutPFbBLRdtXrbWsYwva8esUw0LD87YStJ7T5YCfUGxKjx4/JqadP302+wbNHM2vHDB2
t9rX3lWSdx9OVR++BnV7+wIUcgBZeZBCwIpkb1dS6hUlAx/ANWOGpFfF/0BI/fIK2ckmtnUPbKQS
UYs/wLZwbg2ppIrlwJYG2owC8EXUXoa93JRF+cIVmtGZridVXXBIbeYdXzCSrh7zng9HfEPUXfd0
++AEbgzQA/Nt2Q7IrXzemfIpu3EE8V032sPFdLU82n+rgcdvedeeDCx9Ltp4SxulO2H87Hs4S8mu
XYGvrFF7R5Doih/L23c1tdZ+aMUfhMWoFKik6A==
        </ds:SignatureValue>
        <ds:KeyInfo>
            <ds:X509Data>
                <ds:X509Certificate>
MIIDYTCCAkmgAwIBAgIILDys2FsuNskwDQYJKoZIhvcNAQELBQAwODE2MDQGA1UEAwwtU3VwZXJp
bnRlbmRlbmNpYSBkZSBBZG1pbmlzdHJhY2lvbiBUcmlidXRhcmlhMB4XDTI1MDEyNzAzNTYzMFoX
DTI3MDEyNzAzNTYzMFowKDERMA8GA1UEAwwIODA2OTkxNzAxEzARBgNVBAoMCnNhdC5nb2IuZ3Qw
ggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQC0+3YqVm9vekM1Glv5UQkCCOTdBL+pkHPx
MB9SizrZ4cMU13lobRtK0WkKN4+SbkIXxTNxwToxMxLma4siksI4iVEFGe1F/N3QOeYhn6eeVzB7
faHk1tOfZdZewFRMRsY+C9cEV7T5Fv+zsAPXNXIshE+G95myqrnRfpEzA7jlXV5wrqKMjQbph3ws
ovZZRTI3+pFAktxeEGgyjZNoCPfI9qOkVoZ6bgV2dly0Kff+swbWXOw0JGrxI7hbr8DSOK3d/XXg
2vzwoFUKBqT7EpQF+V+AukmM6erBpFeNxX75pCw5qOtgrM1FSC+vUQLMRL/vefrxjz4nOSrzBbKp
hWNbAgMBAAGjfzB9MAwGA1UdEwEB/wQCMAAwHwYDVR0jBBgwFoAUWIJ2jVgosclDBAK01cheai//
+EowHQYDVR0lBBYwFAYIKwYBBQUHAwIGCCsGAQUFBwMEMB0GA1UdDgQWBBR5+67TCVxTQfV8YWfN
xrUc72FN/DAOBgNVHQ8BAf8EBAMCBeAwDQYJKoZIhvcNAQELBQADggEBADfXregE8knwy8k+d/F9
K3tMDWJhSEFdEFrltkUnCtR3pTJuN6xAuCd64eoeybGwXY0LLBlLc9og3PiUD+GFW9XKjOVvmz/2
qzzlg1vmWD9hIZwNaoaWXMrh7O5Yuq7LaX860MH1ymTRTPh3opkhLW79y4cVCg3eGIq5l7uCsvjR
hM0n041HYE+PcizBgb9Ftn5pqkGemCZIzm9qhW9qpcn1TwwigEuS7K46ceMdNoWcySu1WO6JAuPb
b9dEdHXCZV0NugrKZHTS9zk2QHy0uJkeiFUtBnCEukm8xhUWHBoScnEbumFXSqnhuZMoeDjTQ0RL
PyzcXlBAL2rbU36hWJc=
                </ds:X509Certificate>
            </ds:X509Data>
        </ds:KeyInfo>
        <ds:Object>
            <xades:QualifyingProperties xmlns:xades="http://uri.etsi.org/01903/v1.3.2#"
                                       xmlns:xades141="http://uri.etsi.org/01903/v1.4.1#"
                                       Target="#xmldsig-533cf373-f301-4cf2-8637-be9c008ffebd">
                <xades:SignedProperties Id="xmldsig-533cf373-f301-4cf2-8637-be9c008ffebd-signedprops">
                    <xades:SignedSignatureProperties>
                        <xades:SigningTime>2025-11-04T20:18:50.106Z</xades:SigningTime>
                        <xades:SigningCertificate>
                            <xades:Cert>
                                <xades:CertDigest>
                                    <ds:DigestMethod Algorithm="http://www.w3.org/2001/04/xmlenc#sha256" />
                                    <ds:DigestValue>9uDa8KbXn5kOJDAytksPwhTIZYy2V1QIA3Y7JDXPXmU=</ds:DigestValue>
                                </xades:CertDigest>
                                <xades:IssuerSerial>
                                    <ds:X509IssuerName>CN=Superintendencia de Administracion Tributaria</ds:X509IssuerName>
                                    <ds:X509SerialNumber>3187612681514137289</ds:X509SerialNumber>
                                </xades:IssuerSerial>
                            </xades:Cert>
                            <xades:Cert>
                                <xades:CertDigest>
                                    <ds:DigestMethod Algorithm="http://www.w3.org/2001/04/xmlenc#sha256" />
                                    <ds:DigestValue>dMKFT5GpRMlOOkvm+Eejattj/UGN45s1Ujg/fal4b6w=</ds:DigestValue>
                                </xades:CertDigest>
                                <xades:IssuerSerial>
                                    <ds:X509IssuerName>CN=Superintendencia de Administracion Tributaria</ds:X509IssuerName>
                                    <ds:X509SerialNumber>49188108610261972699090769446035082589</ds:X509SerialNumber>
                                </xades:IssuerSerial>
                            </xades:Cert>
                        </xades:SigningCertificate>
                    </xades:SignedSignatureProperties>
                </xades:SignedProperties>
            </xades:QualifyingProperties>
        </ds:Object>
    </ds:Signature>

    <ds:Signature Id="xmldsig-032fedf0-a2cc-4246-882a-585bcb5cf926">
        <ds:SignedInfo>
            <ds:CanonicalizationMethod Algorithm="http://www.w3.org/TR/2001/REC-xml-c14n-20010315" />
            <ds:SignatureMethod Algorithm="http://www.w3.org/2001/04/xmldsig-more#rsa-sha256" />
            <ds:Reference Id="xmldsig-032fedf0-a2cc-4246-882a-585bcb5cf926-ref0" URI="#DatosCertificados">
                <ds:Transforms>
                    <ds:Transform Algorithm="http://www.w3.org/2000/09/xmldsig#enveloped-signature" />
                </ds:Transforms>
                <ds:DigestMethod Algorithm="http://www.w3.org/2001/04/xmlenc#sha256" />
                <ds:DigestValue>3Hu1UMRYCGcdev47fhzVxfLD7BTBc+/qoo/JthtcirY=</ds:DigestValue>
            </ds:Reference>
            <ds:Reference Type="http://uri.etsi.org/01903#SignedProperties" URI="#xmldsig-032fedf0-a2cc-4246-882a-585bcb5cf926-signedprops">
                <ds:DigestMethod Algorithm="http://www.w3.org/2001/04/xmlenc#sha256" />
                <ds:DigestValue>wAG/GWFHCIqNtOqWMQeSoduLqEk3M3yQo7171ddB38g=</ds:DigestValue>
            </ds:Reference>
        </ds:SignedInfo>
        <ds:SignatureValue Id="xmldsig-032fedf0-a2cc-4246-882a-585bcb5cf926-sigvalue">
CAV6pNh+xNGrWMG1vRq22iHiGJrKxEbZ0xiKAAP1+ikhrZApYOfAD42xDWfPXSVLB66AynC3wjzm
7Whoq3K07AcSTnFlbrndfD1jjIAX8rkReGav0sAKEyF2cJVW576IFm4bl4Az6VjGMG5c1/DnaZX0
9qnaX1SsdBWmBf+XQOJfvuKK+iiv8pfJuF9IhCRnlxBxhKMclmu4ZefsTpT/qSThsSyo5gNAaymr
TYj1YkTczgUCAJAV7sE8YgKMEWNUEVGs6C0xOfNIjrplNtFUx1CPMCPiEBEYkpMknMOrxAztYfFy
SXImNrNDjUX8hiYdXIqZ+gyqHXVNZzdLshuVLw==
        </ds:SignatureValue>
        <ds:KeyInfo>
            <ds:X509Data>
                <ds:X509Certificate>
MIIESTCCAzGgAwIBAgIEEQtBRTANBgkqhkiG9w0BAQsFADCB1DELMAkGA1UEBhMCR1QxMzAxBgNV
BAgTKjdhLiBBdmVuaWRhIDMtNzMgem9uYSA5IEVkaWZpY2lvIFRvcnJlIFNBVDESMBAGA1UEBxMJ
R3VhdGVtYWxhMTYwNAYDVQQKEy1TVVBFUklOVEVOREVOQ0lBIERFIEFETUlOSVNUUkFDSU9OIFRS
SUJVVEFSSUExDDAKBgNVBAsTA1NBVDE2MDQGA1UEAxMtU1VQRVJJTlRFTkRFTkNJQSBERSBBRE1J
TklTVFJBQ0lPTiBUUklCVVRBUklBMB4XDTI1MDcyMTA3MTIxOVoXDTI2MDcyMTA3MTIxOVowgdQx
CzAJBgNVBAYTAkdUMTMwMQYDVQQIEyo3YS4gQXZlbmlkYSAzLTczIHpvbmEgOSBFZGlmaWNpbyBU
b3JyZSBTQVQxEjAQBgNVBAcTCUd1YXRlbWFsYTE2MDQGA1UEChMtU1VQRVJJTlRFTkRFTkNJQSBE
RSBBRE1JTklTVFJBQ0lPTiBUUklCVVRBUklBMQwwCgYDVQQLEwNTQVQxNjA0BgNVBAMTLVNVUEVS
SU5URU5ERU5DSUEgREUgQURNSU5JU1RSQUNJT04gVFJJQlVUQVJJQTCCASIwDQYJKoZIhvcNAQEB
BQADggEPADCCAQoCggEBAIsmT1Dou52w8Z6CGMBGjPjZl81S9PPL344hfaCyT9fX/huPFhr5XqMH
rqv1mgWF9WYSY7t4ILmWHP8oRDbX+8wqUS2a3YcVyqFboIawMplcYPkA2qGaj6IQjIqhP6YdKZkT
9TMZA5WHItLpgMEfJGslM+iGZBpE/e9lBzBr7tB8l1oEpd7cMNf/NV420Qh9yhjj49FPaO/4NnXP
+GVwpWTsjDkh88ueYIZELDhdG0UxfPergnuM/VNgsVTPVjit2yNg6Ikjv87xykglnsxFsd+b2hYs
L0pdGBjWDJOk0AmhBOwRSxniUxiMuq8Gh/Z6tGbFZOzWO3Hb+kv+NvD74LkCAwEAAaMhMB8wHQYD
VR0OBBYEFKOqQ44s6m/nIj1ZmjSRjmLZHbzaMA0GCSqGSIb3DQEBCwUAA4IBAQAdm1CxOrzI6KyT
6sFF+xdeFaKwjnNN+JT+3HX2+21PG7911eq/u7RNjiviM5QSkPfXkTWxfrR8B0U8kJfmFhRhTRPI
oRzJcncMNhM3mTksg5dO1S2NgoMnDNDW8smKrlDyz/DPQKxelLwv2CzRAESnw5+DrI9wWbgnTiej
NfUURwPVYKmAtV1D5geuptK1Oeytn8IMoGURHT4cxt7+6vpHqIyjqzy2kBe3ZubJc5Q9qGny3oVF
tBqO4lKtqXgfavuYElV7Sm2L/e0Z8CHL+KbtVpFSYdQWqUHcAKv0Cc+gf2UwX1ZHAzfMSCYjEqof
xiv019nfjwiyFmGHshJRM9VX
                </ds:X509Certificate>
            </ds:X509Data>
        </ds:KeyInfo>
        <ds:Object>
            <xades:QualifyingProperties xmlns:xades="http://uri.etsi.org/01903/v1.3.2#"
                                       xmlns:xades141="http://uri.etsi.org/01903/v1.4.1#"
                                       Target="#xmldsig-032fedf0-a2cc-4246-882a-585bcb5cf926">
                <xades:SignedProperties Id="xmldsig-032fedf0-a2cc-4246-882a-585bcb5cf926-signedprops">
                    <xades:SignedSignatureProperties>
                        <xades:SigningTime>2025-11-04T20:18:50.194Z</xades:SigningTime>
                        <xades:SigningCertificate>
                            <xades:Cert>
                                <xades:CertDigest>
                                    <ds:DigestMethod Algorithm="http://www.w3.org/2001/04/xmlenc#sha256" />
                                    <ds:DigestValue>fMjM2Q1vJc11L4OrG1ZbNABe9vxUxwuqqVNCui/6MZo=</ds:DigestValue>
                                </xades:CertDigest>
                                <xades:IssuerSerial>
                                    <ds:X509IssuerName>CN=SUPERINTENDENCIA DE ADMINISTRACION TRIBUTARIA,OU=SAT,O=SUPERINTENDENCIA DE ADMINISTRACION TRIBUTARIA,L=Guatemala,ST=7a. Avenida 3-73 zona 9 Edificio Torre SAT,C=GT</ds:X509IssuerName>
                                    <ds:X509SerialNumber>285950277</ds:X509SerialNumber>
                                </xades:IssuerSerial>
                            </xades:Cert>
                        </xades:SigningCertificate>
                    </xades:SignedSignatureProperties>
                </xades:SignedProperties>
            </xades:QualifyingProperties>
        </ds:Object>
    </ds:Signature>
</dte:GTDocumento>

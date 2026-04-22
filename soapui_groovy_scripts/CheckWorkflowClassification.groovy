import groovy.json.JsonSlurper
import javax.naming.Context
import groovy.xml.*

def response = context.expand( '${Retrieve Catalog Items - get Eligible Offers (portfolio)#Response#$[\'ImplRetrieveCatalogItemsResponse\'][\'searchCatalogItems\'][\'results\']}' )

def json = new JsonSlurper().parseText(response)
def len = json.result.size()

log.info "Number of offer IDs: " + len

def testString = "'" + json[0].catalogItemID + "'"
def i = 1

while (i &lt; len) {
    def catalogID = json[i].catalogItemID
    testString = testString + "," + "'" + catalogID + "'"

    i++
}

log.info testString

testRunner.testCase.setPropertyValue( "Pro_Response", testString )
def tCase = testRunner.testCase
def tStep = tCase.testSteps["Get_Workflow_Classification from tbdynamic_bill_prop"]
tStep.run(testRunner, context)

def tRequest_orderActionType = context.expand( '${Retrieve Catalog Items - get Eligible Offers (portfolio)#Request#$.ImplRetrieveCatalogItemsRequest.serviceabilityObject.orderActionType}' )
log.info "Order Action Type for this Order: " + tRequest_orderActionType

def responseAsXml = context.expand( '${Get_Workflow_Classification from tbdynamic_bill_prop#ResponseAsXml#//Results[1]/ResultSet[1]}' )

def envelope = new XmlSlurper().parseText(responseAsXml)

def flag = true

envelope.'**'
    .findAll { it.name() == 'Row' }
    .each {
        if (!it.VALUE.text().contains(tRequest_orderActionType)) {
            log.info "Invalid Offer for this Order : "+ it.ELEMENT_ID.text()
            log.info it.ELEMENT_TYPE.text()
            log.info it.VALUE.text()
            flag = false
        }
    }

assert(flag)

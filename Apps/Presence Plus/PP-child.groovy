/**
 *  **************** Presence Plus Child App  ****************
 *
 *  Design Usage:
 *  Creates a combined presence device that can be used with Life360 Tracker, Google Assistant, Rule Machine and More!
 *
 *  Copyright 2019-2021 Bryan Turcotte (@bptworld)
 * 
 *  This App is free.  If you like and use this app, please be sure to mention it on the Hubitat forums!  Thanks.
 *
 *  Remember...I am not a professional programmer, everything I do takes a lot of time and research!
 *  Donations are never necessary but always appreciated.  Donations to support development efforts are accepted via: 
 *
 *  Paypal at: https://paypal.me/bptworld
 * 
 *  Unless noted in the code, ALL code contained within this app is mine. You are free to change, ripout, copy, modify or
 *  otherwise use the code in anyway you want. This is a hobby, I'm more than happy to share what I have learned and help
 *  the community grow. Have FUN with it!
 * 
 *-------------------------------------------------------------------------------------------------------------------
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 * ------------------------------------------------------------------------------------------------------------------------------
 *
 *  If modifying this project, please keep the above header intact and add your comments/credits below - Thank you! -  @BPTWorld
 *
 *  App and Driver updates can be found at https://github.com/bptworld/Hubitat/
 *
 * ------------------------------------------------------------------------------------------------------------------------------
 *
 *  Changes:
 *
 *  1.1.2 - 08/05/21 - More changes
 *  1.1.1 - 08/03/21 - Many changes
 *  1.1.0 - 06/24/21 - Added IP Ping option, Added Motion to options, Reversed how Contacts work, upgraded device creation and logging options
 *  ---
 *  1.0.0 - 11/01/19 - Initial release.
 *
 */

import groovy.time.TimeCategory
import java.text.SimpleDateFormat
#include BPTWorld.bpt-normalStuff

def setVersion(){
    state.name = "Presence Plus"
	state.version = "1.1.2"
}

definition(
    name: "Presence Plus Child",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "Creates a combined presence device that can be used with Life360 Tracker, Google Assistant, Rule Machine and More!",
    category: "Convenience",
	parent: "BPTWorld:Presence Plus",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
	importUrl: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Apps/Presence%20Plus/PP-child.groovy",
)

preferences {
    page(name: "pageConfig")
}

def pageConfig() {
    dynamicPage(name: "", title: "", install: true, uninstall: true) {
		display() 
        section("${getImage('instructions')} <b>Instructions:</b>", hideable: true, hidden: true) {
			paragraph "<b>Instructions:</b>"
            paragraph "<i>Trigger Type:</i><br>OR - If any selected sensor changes status, then device created will also change<br>AND - If all selected sensors show the same status, the device created will change to that status"
            paragraph "The device created will hold the present/not present value and will also turn on/off which can be used with Goolge Assistant. On = present, Off = not present"
            paragraph "<b>Notes:</b>"
            paragraph "This is a simple way to combine presence sensors. Built with other BPTWorld apps in mind, this will make it easier to expand the functions of other BPTWorld apps."
            paragraph "What ever name you choose for this child app will also be the name of the device automaticaly created."
            paragraph "Also, it's <i>always</i> a good idea to go into the newly created device and set the initial presence state."
        }
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Virtual Device")) {
            createDeviceSection("Presence Plus Driver")
        }
        
		section(getFormat("header-green", "${getImage("Blank")}"+" Present Options")) {
            paragraph "This is the First check. If what is selected here returns true, then Present. Regardless of what the other sections return."
            input "ArrTriggerType", "bool", title: "Trigger Option: Use 'or' or 'and' ('or' = off, 'and' = on)", description: "type", required:false, defualtValue:false, submitOnChange:true
            if(ArrTriggerType) paragraph "<b>using 'AND'</b>"
            if(!ArrTriggerType) paragraph "<b>using 'OR'</b>"
			input "ArrPresenceSensors", "capability.presenceSensor", title: "Presence Sensors to combine (present)", multiple:true, required:false
            input "ArrConPresenceSensors", "capability.contactSensor", title: "Contact Sensors to combine (present when open)", multiple:true, required:false
            input "ArrMotionPresenceSensors", "capability.motionSensor", title: "Motion Sensors to combine (present when active)", multiple:true, required:false
            if(ArrTriggerType) input "arrNumOfSensors", "number", title: "How many sensors does it take to change status for Present (leave blank for All)", required:false, submitOnChange:true 
		}
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Ping Options")) {
            paragraph "PP can also Ping an IP Address to see if it is Present or not. This will be included in both Arrival and Departure options.<br><small>* ONLY available for hub model C-7 running version 2.2.6.140 or above.</small>"
            if(location.hub.firmwareVersionString > "2.2.6.140") {
                input "ipAddress", "text", title: "Enter in IP Addresses", required:false
                input "numPings", "number", title: "Number of Ping attempts (1 to 5)", required:false, range: '1..5'
                input "pingEvery", "enum", title: "Ping every X minutes", description: "pingEvery", required:false, submitOnChange:true, options: ["Every 1 Minute", "Every 5 Minutes", "Every 10 Minutes", "Every 15 Minutes", "Every 30 Minutes", "Every 1 Hour", "Every 3 Hours"]
            } else {
                paragraph "Ping Options are only available for hub model C-7 running version 2.2.6.140 or above."
            }
        }
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Advanced Present Options")) {
            paragraph "Advanced Present Options give you a second set of Present Triggers to choose from.<br>ie. if sensor1 = present -> Present 'else' if sensor2 and sensor3 = present -> Present"
            input "useAdvancedArr", "bool", title: "Use Advanced Present Options", description: "Advanced Present", required:false, submitOnChange:true
            if(useAdvancedArr) {
                input "ArrTriggerType2", "bool", title: "Trigger Option: Use 'or' or 'and' ('or' = off, 'and' = on)", description: "type", required:false, defualtValue:false, submitOnChange:true
                if(ArrTriggerType2) paragraph "<b>using 'AND'</b>"
                if(!ArrTriggerType2) paragraph "<b>using 'OR'</b>"
                input "ArrPresenceSensors2", "capability.presenceSensor", title: "Presence Sensors to combine (present)", multiple:true, required:false
                input "ArrConPresenceSensors2", "capability.contactSensor", title: "Contact Sensors to combine (present when open)", multiple:true, required:false
                if(ArrTriggerType2) input "arrNumOfSensors2", "number", title: "How many sensors does it take to change status for Present (leave blank for All)", required:false, submitOnChange:true 
            } else {
                app.removeSetting("ArrTriggerType2")
                app.removeSetting("ArrPresenceSensors2")
                app.removeSetting("ArrConPresenceSensors2")
                app.removeSetting("arrNumOfSensors2")           
            }
		}
 
        section(getFormat("header-green", "${getImage("Blank")}"+" Not Present Options")) {
            input "DepTriggerType", "bool", title: "Trigger Option: Use 'or' or 'and' ('or' = off, 'and' = on)", description: "type", required:false, submitOnChange:true
			if(DepTriggerType) paragraph "<b>using 'AND'</b>"
            if(!DepTriggerType) paragraph "<b>using 'OR'</b>"
            input "DepPresenceSensors", "capability.presenceSensor", title: "Presence Sensors to combine (not present)", multiple:true, required:false
            input "DepConPresenceSensors", "capability.contactSensor", title: "Contact Sensors to combine (not present when closed)", multiple:true, required:false
            input "DepMotionPresenceSensors", "capability.motionSensor", title: "Motion Sensors to combine (not present when inactive)", multiple:true, required:false
            if(DepTriggerType) input "depNumOfSensors", "number", title: "How many sensors does it take to change status for Not Present (leave blank for All)", required:false, submitOnChange:true
		}
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Failsafe Options")) {
            paragraph "Sometimes a Present or Not Present can be missed. With this option, Presence Plus will check every X minutes to see who is here."
            input "runEvery", "enum", title: "Check every X minutes", description: "runEvery", required:false, submitOnChange:true, options: ["Every 1 Minute", "Every 5 Minutes", "Every 10 Minutes", "Every 15 Minutes", "Every 30 Minutes", "Every 1 Hour", "Every 3 Hours"]
            input "theDelayArr", "number", title: "Delay setting Present status by (seconds)", required:false, submitOnChange:true
            input "theDelayDep", "number", title: "Delay setting Not Present status by (seconds)", required:false, submitOnChange:true
        }
        
        section(getFormat("header-green", "${getImage("Blank")}"+" App Control")) {
            input "pauseApp", "bool", title: "Pause App", defaultValue:false, submitOnChange:true
            if(pauseApp) {
                if(app.label) {
                    if(!app.label.contains("(Paused)")) {
                        app.updateLabel(app.label + " <span style='color:red'>(Paused)</span>")
                    }
                }
            } else {
                if(app.label) {
                    if(app.label.contains("(Paused)")) {
                        app.updateLabel(app.label - " <span style='color:red'>(Paused)</span>")
                    }
                }
            }
        }
        section() {
            paragraph "This app can be enabled/disabled by using a switch. The switch can also be used to enable/disable several apps at the same time."
            input "disableSwitch", "capability.switch", title: "Switch Device(s) to Enable / Disable this app", submitOnChange:true, required:false, multiple:true
        }

        section(getFormat("header-green", "${getImage("Blank")}"+" General")) {
            if(pauseApp) { 
                paragraph app.label
            } else {
                label title: "Enter a name for this automation", required:false
            }
            input "logEnable", "bool", title: "Enable Debug Options", description: "Log Options", defaultValue:false, submitOnChange:true
            if(logEnable) {
                input "logOffTime", "enum", title: "Logs Off Time", required:false, multiple:false, options: ["1 Hour", "2 Hours", "3 Hours", "4 Hours", "5 Hours", "Keep On"]
            }
        }
        display2()
	}
}

def installed() {
    log.debug "Installed with settings: ${settings}"
	initialize()
}

def updated() {	
    if(logEnable) log.debug "Updated with settings: ${settings}"
	unschedule()
    unsubscribe()
    if(logEnable && logOffTime == "1 Hour") runIn(3600, logsOff, [overwrite:false])
    if(logEnable && logOffTime == "2 Hours") runIn(7200, logsOff, [overwrite:false])
    if(logEnable && logOffTime == "3 Hours") runIn(10800, logsOff, [overwrite:false])
    if(logEnable && logOffTime == "4 Hours") runIn(14400, logsOff, [overwrite:false])
    if(logEnable && logOffTime == "5 Hours") runIn(18000, logsOff, [overwrite:false])
    if(logEnagle && logOffTime == "Keep On") unschedule(logsOff)
	initialize()
}

def initialize() {
    checkEnableHandler()
    if(pauseApp) {
        log.info "${app.label} is Paused"
    } else {
        atomicState.runOffDelay = false
        atomicState.runOnDelay = false
        if(ArrPresenceSensors) subscribe(ArrPresenceSensors, "presence", statusUpdateHandler)
        if(DepPresenceSensors) subscribe(DepPresenceSensors, "presence", statusUpdateHandler)
        
        if(ArrPresenceSensors2) subscribe(ArrPresenceSensors2, "presence", statusUpdateHandler)
        
        if(ArrConPresenceSensors) subscribe(ArrConPresenceSensors, "contact", statusUpdateHandler)        
        if(DepConPresenceSensors) subscribe(DepConPresenceSensors, "contact", statusUpdateHandler)
        if(DepMotionPresenceSensors) subscribe(DepMotionPresenceSensors, "motion", statusUpdateHandler)
        if(DepMotionPresenceSensors) subscribe(DepMotionPresenceSensors, "motion", statusUpdateHandler)

        if(runEvery == "Every 1 Minute") runEvery1Minute(statusUpdateHandler)
        if(runEvery == "Every 5 Minutes") runEvery5Minutes(statusUpdateHandler)
        if(runEvery == "Every 10 Minutes") runEvery10Minutes(statusUpdateHandler)
        if(runEvery == "Every 15 Minutes") runEvery15Minutes(statusUpdateHandler)
        if(runEvery == "Every 30 Minutes") runEvery30Minutes(statusUpdateHandler)
        if(runEvery == "Every 1 Hour") runEvery1Hour(statusUpdateHandler)
        if(runEvery == "Every 3 Hours") runEvery3Hours(statusUpdateHandler)
        
        if(pingEvery == "Every 1 Minute") runEvery1Minute(pingHandler)
        if(pingEvery == "Every 5 Minutes") runEvery5Minutes(pingHandler)
        if(pingEvery == "Every 10 Minutes") runEvery10Minutes(pingHandler)
        if(pingEvery == "Every 15 Minutes") runEvery15Minutes(pingHandler)
        if(pingEvery == "Every 30 Minutes") runEvery30Minutes(pingHandler)
        if(pingEvery == "Every 1 Hour") runEvery1Hour(pingHandler)
        if(pingEvery == "Every 3 Hours") runEvery3Hours(pingHandler)
        if(ipAddress) pingHandler()
    }
}

def arrSensorHandler(evt) {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(evt) {
            whoHappened = evt.displayName
            whatHappened = evt.value
        } else {
            whoHappened = "NA"
            whatHappened = "NA"
        }
        state.pStatus1 = false
        if(ArrTriggerType == null || ArrTriggerType == "") ArrTriggerType = false
        if(ArrTriggerType2 == null || ArrTriggerType2 == "") ArrTriggerType2 = false
        if(logEnable) log.debug "----- In arrSensorHandler (${state.version}) -----"
        if(logEnable) log.debug "In arrSensorHandler - ArrTriggerType: ${ArrTriggerType} - ArrTriggerType2: ${ArrTriggerType2}"
        if(logEnable) log.debug "In arrSensorHandler - whoHappened: ${whoHappened} - whatHappened: ${whatHappened}"	

        int theDelayArr = theDelayArr ?: 1
        int pCount = 0
        int pCount2 = 0

        if(ArrPresenceSensors) {
            preSensors = ArrPresenceSensors.size()
        } else {
            preSensors = 0
        }
        if(ArrConPresenceSensors) {
            conSensors = ArrConPresenceSensors.size()
        } else {
            conSensors = 0
        }
        if(ArrMotionPresenceSensors) {
            motSensors = ArrMotionPresenceSensors.size()
        } else {
            motSensors = 0
        }
        if(ipAddress) {
            ipSensors = 1
        } else {
            ipSensors = 0
        }
        if(logEnable) log.debug "In arrSensorHandler - preSensors: $preSensors - conSensors: $conSensors - motSensors: $motSensors - ipSensors: $ipSensors"
        asCount = preSensors + conSensors + + motSensors + ipSensors
        int theArrNum = arrNumOfSensors ?: asCount
        if(logEnable) log.debug "In arrSensorHandler - asCount: $asCount - arrNumOfSensors: $arrNumOfSensors - theArrNum: $theArrNum"

        if(ArrPresenceSensors2) {
            preSensors2 = ArrPresenceSensors2.size()
        } else {
            preSensors2 = 0
        }
        if(ArrConPresenceSensors2) {
            conSensors2 = ArrConPresenceSensors2.size()
        } else {
            conSensors2 = 0
        }
        asCount2 = preSensors2 + conSensors2
        int theArrNum2 = arrNumOfSensors2 ?: asCount2
        if(logEnable) log.debug "In arrSensorHandler - asCount2: $asCount2 - arrNumOfSensors2: $arrNumOfSensors2 - theArrNum2: $theArrNum2"

        if(ArrTriggerType == false) {    // or
            //if(logEnable) log.debug "In arrSensorHandler - Arr: ${ArrTriggerType} - Should be FALSE for OR handler"
            if(ArrPresenceSensors) {
                ArrPresenceSensors.each { it ->
                    if(it.currentValue("presence") == "present") {
                        state.pStatus1 = true	
                    }
                }
            }
            if(ArrConPresenceSensors) {
                ArrConPresenceSensors.each { it ->
                    if(it.currentValue("contact") == "open") {
                        state.pStatus1 = true	
                    }
                }
            }
            if(ArrMotPresenceSensors) {
                ArrMotPresenceSensors.each { it ->
                    if(it.currentValue("motion") == "active") {
                        state.pStatus1 = true	
                    }
                }
            }
            if(ipAddress) {
                if(state.ipStatus == "present") {
                    state.pStatus1 = true
                }
            }
        }

        if(ArrTriggerType == true) {    // and
            //if(logEnable) log.debug "In arrSensorHandler - Arr: ${ArrTriggerType} - Should be TRUE for AND handler"
            if(ArrPresenceSensors) {
                ArrPresenceSensors.each { it ->
                    if(it.currentValue("presence") == "present") {
                        pCount = pCount + 1	
                    }
                }
            }
            if(ArrConPresenceSensors) {
                ArrConPresenceSensors.each { it ->
                    if(it.currentValue("contact") == "open") {
                        pCount = pCount + 1	
                    }
                }
            }
            if(ArrMotPresenceSensors) {
                ArrMotPresenceSensors.each { it ->
                    if(it.currentValue("motion") == "active") {
                        pCount = pCount + 1	
                    }
                }
            }
            if(ipAddress) {
                if(state.ipStatus == "present") {
                    pCount = pCount + 1
                }
            }
            
            if(pCount >= theArrNum) state.pStatus1 = true
            if(logEnable) log.debug "In arrSensorHandler - Arr - sensorCount: ${asCount} - presentCount: ${pCount} - theArrNum: ${theArrNum} -- pStatus: ${state.pStatus1}"
        }

        if(useAdvancedArr) {
            if(ArrTriggerType2 == false) {    // or
                //if(logEnable) log.debug "In arrSensorHandler - Arr2: ${ArrTriggerType2} - Should be FALSE for OR handler"
                if(ArrPresenceSensors2) {
                    ArrPresenceSensors2.each { it ->
                        if(it.currentValue("presence") == "present") {
                            state.pStatus1 = true	
                        }
                    }
                }
                if(ArrConPresenceSensors2) {
                    ArrConPresenceSensors2.each { it ->
                        if(it.currentValue("contact") == "open") {
                            state.pStatus1 = true	
                        }
                    }
                }
            }

            if(ArrTriggerType2 == true) {    // and
                //if(logEnable) log.debug "In arrSensorHandler - Arr2: ${ArrTriggerType2} - Should be TRUE for AND handler"
                if(ArrPresenceSensors2) {
                    ArrPresenceSensors2.each { it ->
                        if(it.currentValue("presence") == "present") {
                            pCount2 = pCount2 + 1	
                        }
                    }
                }
                if(ArrConPresenceSensors2) {
                    ArrConPresenceSensors2.each { it ->
                        if(it.currentValue("contact") == "open") {
                            pCount2 = pCount2 + 1	
                        }
                    }
                }
                
                if(pCount2 >= theArrNum2) state.pStatus1 = true
                if(logEnable) log.debug "In arrSensorHandler - Adv Arr - sensorCount: ${asCount} - presentCount: ${pCount} - theArrNum: ${theArrNum} -- pStatus: ${state.pStatus1}"
            }
        }
        if(logEnable) log.debug "----- In arrSensorHandler - Returns: ${state.pStatus1} -----"
    }
}

def depSensorHandler(evt) {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(evt) {
            whoHappened = evt.displayName
            whatHappened = evt.value
        } else {
            whoHappened = "NA"
            whatHappened = "NA"
        }
        state.pStatus2 = false
        if(DepTriggerType == null || DepTriggerType == "") DepTriggerType = false
        if(logEnable) log.debug "----- In depSensorHandler (${state.version}) -----"
        if(logEnable) log.debug "In depSensorHandler - DepTriggerType: ${DepTriggerType}"	
        if(logEnable) log.debug "In arrSensorHandler - whoHappened: ${whoHappened} - whatHappened: ${whatHappened}"	

        if(DepPresenceSensors) {
            depSensors = DepPresenceSensors.size()
        } else {
            depSensors = 0
        }
        if(DepConPresenceSensors) {
            depConSensors = DepConPresenceSensors.size()
        } else {
            depConSensors = 0
        }
        if(DepMonPresenceSensors) {
            depMonSensors = DepMonPresenceSensors.size()
        } else {
            depMonSensors = 0
        }
        if(ipAddress) {
            depIpSensors = 1
        } else {
            depIpSensors = 0
        }
        dsCount = depSensors + depConSensors + depMonSensors + depIpSensors
        int theDelayDep = theDelayDep ?: 1
        int theDepNum = depNumOfSensors ?: dsCount
        int pCount = 0

        if(DepTriggerType == false) {    // or
            //if(logEnable) log.debug "In depSensorHandler - Dep: ${DepTriggerType} - Should be FALSE for OR handler"
            if(DepPresenceSensors) {
                DepPresenceSensors.each { it ->
                    if(it.currentValue("presence") == "not present") {
                        state.pStatus2 = true	
                    }
                }
            }
            if(DepConPresenceSensors) {
                DepConPresenceSensors.each { it ->
                    if(it.currentValue("contact") == "closed") {
                        state.pStatus2 = true	
                    }
                }
            }
            if(DepMotPresenceSensors) {
                DepMotPresenceSensors.each { it ->
                    if(it.currentValue("motion") == "inactive") {
                        state.pStatus2 = true	
                    }
                }
            }
            if(ipAddress) {
                if(state.ipStatus == "not present") {
                    state.pStatus2 = true
                }
            }
        }

        if(DepTriggerType == true) {    // and
            //if(logEnable) log.debug "In depSensorHandler - Dep: ${DepTriggerType} - Should be TRUE for AND handler"
            if(DepPresenceSensors) {
                DepPresenceSensors.each { it ->
                    if(it.currentValue("presence") == "not present") {
                        pCount = pCount + 1	
                    }
                }
            }
            if(DepConPresenceSensors) {
                DepConPresenceSensors.each { it ->
                    if(it.currentValue("contact") == "closed") {
                        pCount = pCount + 1	
                    }
                }
            }
            if(DepMonPresenceSensors) {
                DepMonPresenceSensors.each { it ->
                    if(it.currentValue("motion") == "inactive") {
                        pCount = pCount + 1	
                    }
                }
            }
            if(ipAddress) {
                if(state.ipStatus == "not present") {
                    pCount = pCount + 1
                }
            }
            if(logEnable) log.debug "In depSensorHandler - Dep - sensorCount: ${dsCount} - notPresentCount: ${pCount} - theDepNum: ${theDepNum}"
            if(pCount >= theDepNum) state.pStatus2 = true       
        }
        if(logEnable) log.debug "----- In depSensorHandler - Returns: ${state.pStatus2} -----"
    }
}

def statusUpdateHandler(evt) {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(logEnable) log.debug "----- Starting Presence Plus - ${app.label} -----"
        arrSensorHandler(evt)
        depSensorHandler(evt)       
        if(logEnable) log.debug "In statusUpdateHandler (${state.version}) - pStatus1: ${state.pStatus1} - pStatus2: ${state.pStatus2}"
        if(state.pStatus1 == true) {
            if(atomicState.runOffDelay == true) {
                unschedule(runOffDelay)
            }
            theDelay = theDelayArr ?: 1
            if(logEnable) log.debug "In statusUpdateHandler - Arr - Will set status to ${state.pStatus1} after a ${theDelay} second delay"
            atomicState.runOnDelay = true
            runIn(theDelay, switchOn, [overwrite:false])
        } else if(state.pStatus2 == true) {
            if(atomicState.runOnDelay == true) {
                unschedule(runOnDelay)
            }
            theDelay = theDelayDep ?: 1
            if(logEnable) log.debug "In statusUpdateHandler - Dep - Will set status to ${state.pStatus2} after a ${theDelay} second delay"
            atomicState.runOffDelay = true
            runIn(theDelay, switchOff, [overwrite:false])
        } else {
            if(logEnable) log.debug "In statusUpdateHandler - Neither Arr or Dep returned True. Please check your settings."
        }
        if(logEnable) log.debug "----- Finisehd with Presence Plus - ${app.label} -----"
    }
}

def switchOn() {
    try {
        def mySensorStatus = dataDevice.currentValue("switch")
        if(logEnable) log.debug "In switchOn - Sending ON for Present if needed (switch is ${mySensorStatus})"
        if(mySensorStatus == "off" || mySensorStatus == null) dataDevice.on()
        atomicState.runOnDelay = false
    } catch (e) {
        log.warn "In switchOn - pStatus1 - Something went wrong"
    }
}

def switchOff() {
    try {
        def mySensorStatus = dataDevice.currentValue("switch")
        if(logEnable) log.debug "In switchOff - Sending OFF for Not Present if needed (switch is ${mySensorStatus})"
        if(mySensorStatus == "on" || mySensorStatus == null) dataDevice.off()
        atomicState.runOffDelay = false
    } catch (e) {
        log.warn "In switchOff - pStatus2 - Something went wrong"
    } 
}

def pingHandler() {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(logEnable) log.debug "In pingHandler (${state.version}) - firmwareVersion: $location.hub.firmwareVersionString (Needs to be above 2.2.6.140)"
        if(logEnable) log.debug "In pingHandler - Trying: ${ipAddress}"
        hubitat.helper.NetworkUtils.PingData pingData = hubitat.helper.NetworkUtils.ping(ipAddress, numPings.toInteger())
        int pTran = pingData.packetsTransmitted.toInteger()
        if (pTran == 0){ // 2.2.7.121 bug returns all zeroes on not found per @thebearmay
            pingData.packetsTransmitted = numPings
            pingData.packetLoss = 100
        }
        if(logEnable) log.debug "In pingHandler - Pinging $ipAddress - Transmitted: ${pingData.packetsTransmitted}, Received: ${pingData.packetsReceived}, %Lost: ${pingData.packetLoss}"

        if(pingData.packetLoss < 100) {
            if(logEnable) log.debug "In pingHandler - Present"
            state.ipStatus = "present"
        } else {
            if(logEnable) log.debug "In pingHandler - Not Present"
            state.ipStatus = "not present"
        }
    }
}

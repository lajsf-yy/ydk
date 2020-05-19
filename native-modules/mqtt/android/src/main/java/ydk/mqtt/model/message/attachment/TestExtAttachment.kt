package ydk.mqtt.model.message.attachment

import org.json.JSONObject

class TestExtAttachment : ExtAttachment {

    var message: String = ""

    override fun getMsgType() = "test"


    override fun parseData(josn: String) {
        var jSONObject = JSONObject(josn)
        message = jSONObject.optString("message", "")

    }

    override fun dataPase(): String {

        var jSONObject = JSONObject()

        jSONObject.put("message", message)

        return jSONObject.toString()
    }


}

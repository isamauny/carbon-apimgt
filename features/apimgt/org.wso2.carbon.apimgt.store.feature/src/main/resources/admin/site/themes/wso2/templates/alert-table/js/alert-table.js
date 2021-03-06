$( document ).ready(function() {

    var statsEnabled = isDataPublishingEnabled();
    var table = null;

    var selectedOptionKey = $("#alertSelected option:selected" ).val();
    var tableName = null;

    changeActiveTableName();

    if(statsEnabled){
        drawTable();
    } else {
        $('.content-data').empty();
        $('.content-data').append('<h2> Stats are disabled.</h2>');
    }

    $("#alertSelected").change( function() {
        selectedOptionKey = $("#alertSelected option:selected" ).val();
        changeActiveTableName();
        if(statsEnabled){
            table.ajax.reload(null, true);
        } else{
            $('.content-data').empty();
            $('.content-data').append('<h2> Stats are disabled.</h2>');
        }
    });

    $('#alertHistoryTable_filter input').unbind();
    $('#alertHistoryTable_filter input').bind('keyup', function(e) {
        if(e.keyCode == 13) {
            table.search( this.value ).draw();
        }
    });




    function changeActiveTableName() {
        if(selectedOptionKey == 1){
            tableName = "ORG_WSO2_ANALYTICS_APIM_ALLAPIMALERTSSTREAM"
        } else if(selectedOptionKey== 2){
            tableName = "ORG_WSO2_ANALYTICS_APIM_ALERT_UNUSUALIPACCESS";
        } else if(selectedOptionKey== 3){
            tableName = "ORG_WSO2_ANALYTICS_APIM_ABNORMALREQUESTSPERMINALERTSTREAM";
        } else if(selectedOptionKey== 4){
            tableName = "ORG_WSO2_ANALYTICS_APIM_ABNORMALRESPONSETIMEALERTSTREAM";
        } else if(selectedOptionKey== 5){
            tableName = "ORG_WSO2_ANALYTICS_APIM_ALERT_ABNORMALTIERUSAGEALERT";
        } else if(selectedOptionKey== 6){
            tableName = "ORG_WSO2_ANALYTICS_APIM_ALERT_ABNORMALTOKENREFRESH";
        } else if(selectedOptionKey== 7){
            tableName = "ORG_WSO2_ANALYTICS_APIM_TIERLIMITHITTINGALERT";
        } else if(selectedOptionKey== 8){
            tableName = "ORG_WSO2_ANALYTICS_APIM_ABNORMALBACKENDTIMEALERTSTREAM";
        } else if(selectedOptionKey== 9){
            tableName = "ORG_WSO2_ANALYTICS_APIM_APIHEALTHMONITORALERTSTREAM";
        } else if(selectedOptionKey== 10){
            tableName = "ORG_WSO2_ANALYTICS_APIM_REQUESTPATTERNCHANGEDSTREAM";
        }

    }

    function isDataPublishingEnabled() {
        var isStatsEnabled;

        jagg.syncPost("/site/blocks/alert-table/ajax/alert-table.jag", { action: "isDataPublishingEnabled"},
            function (json) {
                if (!json.error) {
                    isStatsEnabled = json.usage;
                } else {
                    if (json.message == "AuthenticateError") {
                        jagg.showLogin();
                    } else {
                        jagg.message({content: json.message, type: "error"});
                    }
                }
            }, "json");

        return isStatsEnabled;
    }

    function changeSelectedAlertType(alertType) {
        if(alertType != null){
            if(alertType == "Unseen Source IP Address"){
                $('#alertSelected').val(2).change();
            } else if(alertType == "Abnormal Request Count"){
                $('#alertSelected').val(3).change();
            } else if(alertType == "Abnormal Response Time"){
                $('#alertSelected').val(4).change();
            } else if(alertType == "Abnormal API Usage"){
                $('#alertSelected').val(5).change();
            } else if(alertType == "Abnormal Renewal of Access Tokens"){
                $('#alertSelected').val(6).change();
            } else if(alertType == "Tier Crossing"){
                $('#alertSelected').val(7).change();
            } else if(alertType == "Abnormal Backend Time"){
                $('#alertSelected').val(8).change();
            } else if(alertType == "Availability of APIs"){
                $('#alertSelected').val(9).change();
            } else if(alertType == "Abnormal Resource Access Pattern"){
                $('#alertSelected').val(10).change();
            }
        }
    }

    function drawTable() {

        table =$('#alertHistoryTable').datatables_extended({
            "order": [[ 0, "desc" ]],
            //"processing": true,
            "serverSide": true,
            "columns" : [
                { title: "Alert Timestamp" },
                { title: "Type", "orderable": false },
                { title: "Message" , "orderable": false ,"render":function(data){
                    var userId = data['userId'];
                    var applicationName = data['applicationName'];
                    var applicationOwner = data['applicationOwner'];
                    var applicationId = data['applicationId'];
                    var api_version = data['api_version'];
                    var ip = data['ip'];
                    var api = data['api'];
                    var apiPublisher = data['apiPublisher'];
                    var resourceTemplate = data['resourceTemplate'];
                    var method = data['method'];
                    var backendTime = data['backendTime'];
                    var requestPerMin = data['requestPerMin'];
                    var reason = data['reason'];
                    var scope = data['scope'];
                    var consumerKey = data['consumerKey'];

                    var fullMessage = "";;

                    if (userId != null) {
                        fullMessage = '<span class="label label-primary add-margin-right-1x">User ID</span>'+userId+'<br/>';
                    }
                    if (applicationName != null) {
                        fullMessage = fullMessage + '<span class="label label-primary add-margin-right-1x">Application Name</span>'+applicationName+'<br/>';
                    }
                    if (applicationOwner != null) {
                        fullMessage = fullMessage + '<span class="label label-primary add-margin-right-1x">Application Owner</span>'+applicationOwner+'<br/>';
                    }
                    if (applicationId != null) {
                        fullMessage = fullMessage + '<span class="label label-primary add-margin-right-1x">Application Id</span>'+applicationId+'<br/>';
                    }
                    if (api_version != null) {
                        fullMessage = fullMessage + '<span class="label label-primary add-margin-right-1x">API Version</span>'+api_version+'<br/>';
                    }
                    if (ip != null) {
                        fullMessage = fullMessage + '<span class="label label-primary add-margin-right-1x">IP</span>'+ip+'<br/>';
                    }
                    if (api != null) {
                        fullMessage = fullMessage + '<span class="label label-primary add-margin-right-1x">API</span>'+api+'<br/>';
                    }
                    if (apiPublisher != null) {
                        fullMessage = fullMessage + '<span class="label label-primary add-margin-right-1x">API Publisher</span>'+apiPublisher+'<br/>';
                    }
                    if (resourceTemplate != null) {
                        fullMessage = fullMessage + '<span class="label label-primary add-margin-right-1x">Resource Template</span>'+resourceTemplate+'<br/>';
                    }
                    if (method != null) {
                        fullMessage = fullMessage + '<span class="label label-primary add-margin-right-1x">Method</span>'+method+'<br/>';
                    }
                    if (backendTime != null) {
                        fullMessage = fullMessage + '<span class="label label-primary add-margin-right-1x">Backend Time</span>'+backendTime+'<br/>';
                    }
                    if (requestPerMin != null) {
                        fullMessage = fullMessage + '<span class="label label-primary add-margin-right-1x">Requests Per-Min</span>'+requestPerMin+'<br/>';
                    }
                    if (reason != null) {
                        fullMessage = fullMessage + '<span class="label label-primary add-margin-right-1x">Reason</span>'+reason+'<br/>';
                    }
                    if (scope != null) {
                        fullMessage = fullMessage + '<span class="label label-primary add-margin-right-1x">Scope</span>'+scope+'<br/>';
                    }
                    if (consumerKey != null) {
                        fullMessage = fullMessage + '<span class="label label-primary add-margin-right-1x">Consumer Key</span>'+consumerKey+'<br/>';
                    }

                    fullMessage = fullMessage + data['msg'];
                    return fullMessage;
                }
                },
                {title: "Severity", "render": function(data){
                    var severityLabel;
                    if(data == 1) {
                        severityLabel = '<span class="label label-danger">severe</span>';
                    } else if (data == 2) {
                        severityLabel = '<span class="label label-warning">moderate</span>';
                    } else if (data == 3){
                        severityLabel = '<span class="label label-default">mild</span>';
                    } else {
                        severityLabel = '<span class="label label-default">mild</span>';
                    }
                    return severityLabel;
                }
                }
            ],
            ajax: {
                "url" : "site/blocks/alert-table/ajax/alert-table.jag",
                "type": "POST",
                "data" : function (d) {
                    d.action = "getDataFromTable";
                    d.tableName = tableName;
                    d.searchQuery = null;
                    d.entriesPerPage = $("#alertHistoryTable_length option:selected" ).val();
                },
                error:function(xhr,status,error){
                    console.log('Error while trying to connect to the DAS endpoint');
                }
            },
            "drawCallback": function(){
                $('.alertTypeLink').click(function(){
                    changeSelectedAlertType(this.text);
                });

            }
        });

    }


});
     
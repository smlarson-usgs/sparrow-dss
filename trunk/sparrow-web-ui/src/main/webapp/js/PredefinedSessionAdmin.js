/*!
 * Ext JS Library 3.3.1
 * Copyright(c) 2006-2010 Sencha Inc.
 * licensing@sencha.com
 * http://www.sencha.com/license
 */

// Typical JsonReader.  Notice additional meta-data params for defining the core attributes of your json-response
var reader = new Ext.data.XmlReader({
    record : 'PredefinedSession'
        /*,success: '@success'*/
        }, [
            'id'
            ,'modelId'
        	,'predefinedSessionType'
        	,'approved'
        	,'name'
        	,'groupName'
        	,'description'
        	,'sortOrder'
        	,{name: 'addDate', type:'date',  dateFormat:'Y-m-d', mapping:'addDate'}
        	,'addBy'
        	,'addNote'
        	,'addContactInfo'
        	,'contextString'
    ]);

// The new DataWriter component.
var writer = new Ext.data.XmlWriter({
    xmlEncoding: 'UTF-8'
});

//Create a standard HttpProxy instance.
var proxy = new Ext.data.HttpProxy({
    url: 'sample_pre_session.xml'
});

// Typical Store collecting the Proxy, Reader and Writer together.
var store = new Ext.data.Store({
    storeId: 'PredefinedSessionStore',
    restful: false,
    //proxy: proxy,
    url: 'sample_pre_session.xml',
    reader: reader,
    writer: writer
});



function configForEditPage(sessionID){

    Ext.QuickTips.init();

    // turn on validation errors beside the field globally
    Ext.form.Field.prototype.msgTarget = 'side';

    var fs = new Ext.FormPanel({
        frame: true,
        title:'Create / Edit Predefined Session Form',
        labelAlign: 'right',
        labelWidth: 85,
        width:340,
        waitMsgTarget: true,
        items: [
            new Ext.form.FieldSet({
                title: 'Basic Information',
                autoHeight: true,
                defaultType: 'textfield',
                items: [{
                        fieldLabel: 'Name',
                        /* emptyText: 'Predefined session name', */
                        name: 'name',
                        width:190
                    }, {
                        fieldLabel: 'Description',
                        emptyText: 'Small paragraph description of the session',
                        name: 'description',
                        width:190
                    }, {
                        fieldLabel: 'Model ID',
                        name: 'modelId',
                        width:190
                    }, {
                        fieldLabel: 'Unique ID',
                        emptyText: 'Create an ID',
                        name: 'id',
                        width:190
                    },

                    new Ext.form.ComboBox({
                        fieldLabel: 'Type',
                        hiddenName:'predefinedSessionType',
                        store: new Ext.data.ArrayStore({
                            fields: ['name'],
                            data : [['FEATURED'], ['LISTED'], ['UNLISTED']]
                        }),
                        editable: false,
                        valueField:'name',
                        displayField:'name',
                        mode: 'local',
                        triggerAction: 'all',
                        emptyText:'Choose a type...',
                        selectOnFocus:true,
                        width:190
                    }), {
                        fieldLabel: 'Date Added',
                        name: 'addDate',
                        disabled: true,
                        width:190
                    }
                ]
            })
        ]
    });

    // simple button add
    fs.addButton('Load', function(){
    	//fs.getForm().load({url:'sample_pre_session.xml', waitMsg:'Loading'});
    	
//    	var record = store.getAt(0);
//    	fs.getForm().loadRecord(record);
        //fs.getForm().load({url:'sample_pre_session.xml', waitMsg:'Loading'});
    	
    	store.load({
    	    // store loading is asynchronous, use a load listener or callback to handle results
    	    callback: function(){
    	        Ext.Msg.show({
    	            title: 'Store Load Callback',
    	            msg: 'store was loaded, data available for processing.  count: ' + store.getCount(),
    	            modal: false,
    	            icon: Ext.Msg.INFO,
    	            buttons: Ext.Msg.OK
    	        });
    	    	var record = store.getAt(0);
    	    	fs.getForm().loadRecord(record);
    	    	submit.enable();
    	    }
    	});
    });

    // explicit add
    var submit = fs.addButton({
        text: 'Submit',
        disabled:true,
        handler: function(){
    		fs.getForm().updateRecord(store.getAt(0));
    		store.save();
        }
    });

    fs.render('create_edit_form');

    fs.on({
        actioncomplete: function(form, action){
            if(action.type == 'load'){
                submit.enable();
                //fs.getForm().items['addDate'].diabled = true;
            }
        }
    });

}


function loadAllModels() {
    var xmlreq = ''
        + '<?xml version="1.0" encoding="ISO-8859-1" ?>'
        + '<sparrow-meta-request '
        + '  xmlns="http://www.usgs.gov/sparrow/meta_request/v0_1" '
        + '  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">'
        + '  <model public="$public$" archived="$archived$" approved="$approved$" />'
        + '</sparrow-meta-request>'
        ;

    
    var nonpublic = false;
    var nonapproved = false;
    var archived = false;
    
    if (document.getElementById('model-controls-area')) {
    	nonpublic = document.getElementById('model-controls-show-nonpublic').checked
    	nonapproved = document.getElementById('model-controls-show-nonapproved').checked
    	archived = document.getElementById('model-controls-show-archived').checked	
    }
    
    xmlreq = xmlreq.replace("$public$", !nonpublic);
    xmlreq = xmlreq.replace("$approved$", !nonapproved);	
    xmlreq = xmlreq.replace("$archived$", archived);	
    
    
    // Send a request to the model service for a list of public models
    Ext.Ajax.request({
        url: '../getModels',
        params: 'xmlreq=' + xmlreq + '&mimetype=json',
        success: function(result, request) {
    		renderModelList(result.responseText);
        },
        failure: function(response, options) {
            Ext.MessageBox.alert('Failed', 'Unable to connect to SPARROW.');
        }
    });
}

/*
 * Callback function for the model request.  This function renders the model
 * select box adding an option for each model returned.
 */
function renderModelList(modelResponse) {

    // Pull back the response
    modelResponse = Ext.util.JSON.decode(modelResponse);

    var listTable = document.getElementById('pre-session-admin-list-body');
    listTable.innerHTML = '';
    var html = '';
    for (var i = 0; i < modelResponse.models.model.length; i++) {
    	var model = modelResponse.models.model[i];

    	if (model.sessions && model.sessions.session) {
        	
    		var sessions =  model.sessions.session;
    		for (var j = 0; j < sessions.length; j++){
    			var session = sessions[j];
    			
            	html += '<tr>' +
        	    	'<td>' + model['@id'] + '</td>';
            	
    			html += '<td>' + session['@key'] + '</td>';
    			html += '<td>' + session['@name'] + '</td>';
    			html += '<td>' + session['@group_name'] + '</td>';
    			html += '<td>' + session['@type'] + '</td>';
    			html += '<td>' + session['@approved'] + '</td>';
    			html += '<td>' + session['@sort_order'] + '</td>';
    			html += '<td>' + session['@add_by'] + '</td>';
    			html += '<td>' + session['@add_date'] + '</td>';
    			html += '<td>' + session['@description'] + '</td>';
    			
    			html += '</tr>';
    		}

    	}

    	listTable.innerHTML += html;

    }
}

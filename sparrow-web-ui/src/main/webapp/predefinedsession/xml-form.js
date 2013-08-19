/*!
 * Ext JS Library 3.3.1
 * Copyright(c) 2006-2010 Sencha Inc.
 * licensing@sencha.com
 * http://www.sencha.com/license
 */

function configForm() {
	

    Ext.QuickTips.init();

    // turn on validation errors beside the field globally
    Ext.form.Field.prototype.msgTarget = 'side';

    var fs = new Ext.FormPanel({
        frame: true,
        title:'XML Form',
        labelAlign: 'right',
        labelWidth: 85,
        width:340,
        waitMsgTarget: true,

        // configure how to read the XML Data
        reader : new Ext.data.XmlReader({
            record : 'PredefinedSession'
            /*success: '@success'*/
        }, [
            {name: 'name', mapping:'name'}

        ]),

        // reusable eror reader class defined at the end of this file
        errorReader: new Ext.form.XmlErrorReader(),

        items: [
            new Ext.form.FieldSet({
                title: 'Contact Information',
                autoHeight: true,
                defaultType: 'textfield',
                items: [
                	{
                        fieldLabel: 'Name',
                        /* emptyText: 'Predefined session name', */
                        name: 'name',
                        width:190
                    }
                ]
            })
        ]
    });

    // simple button add
    fs.addButton('Load', function(){
        fs.getForm().load({url:'sample_pre_session.xml', waitMsg:'Loading'});
    });

    // explicit add
    var submit = fs.addButton({
        text: 'Submit',
        disabled:true,
        handler: function(){
            fs.getForm().submit({url:'xml-errors.xml', waitMsg:'Saving Data...', submitEmptyText: false});
        }
    });
	
    fs.render('form-ct');

    fs.on({
        actioncomplete: function(form, action){
            if(action.type == 'load'){
                submit.enable();
            }
        }
    });
}






// A reusable error reader class for XML forms
Ext.form.XmlErrorReader = function(){
    Ext.form.XmlErrorReader.superclass.constructor.call(this, {
            record : 'field',
            success: '@success'
        }, [
            'id', 'msg'
        ]
    );
};
Ext.extend(Ext.form.XmlErrorReader, Ext.data.XmlReader);
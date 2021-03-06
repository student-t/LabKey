/*
 * Copyright (c) 2017 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
/**
 * Created by lkacimi on 4/14/2017.
 */
Ext4.define('InstitutionFormPanel', {
    extend: "Ext.form.Panel",
    alias: "widget.institution-form-panel",
    id: 'institution-form-panel',
    bodyStyle: 'padding:5px 5px 0',
    labelWidth: 450,
    defaults: {width: 445},

    items: [
        {
            xtype: 'textfield',
            name: 'institutionId',
            fieldLabel: 'ID',
            disabled: true
        },
        {
            xtype: 'textfield',
            name: 'institutionName',
            fieldLabel: 'Institution Name',
            allowBlank: false
        },
        {
            xtype: 'textfield',
            name: 'shortName',
            fieldLabel: 'Short Name',
            allowBlank: false
        },
        {
            xtype: 'textfield',
            name: 'city',
            fieldLabel: 'City',
            allowBlank: false
        },
        {
            xtype: 'combo',
            fieldLabel: 'State',
            name: 'state',
            store: Ext4.create('StatesStore'),
            valueField: 'code',
            displayField: 'name',
            allowBlank: false,
            typeAhead: true,
            queryMode: 'local'
        },
        {
            xtype: 'textfield',
            name: 'affiliate',
            fieldLabel: 'Affiliate',

        },
        {
            xtype: 'textfield',
            name: 'webSite',
            fieldLabel: 'Web Site',
            vtype: 'url'
        }
    ],
    buttons: [
        {
            text: 'Submit',
            handler: function () {

                this.up('form').updateRecord(this.up('form').getRecord());
                if (!this.up('form').isValid()) {
                    return;
                }
                var window = this.up('window');

                this.up('form').getRecord().save({
                    callback: function (record, response) {
                        if (response.success) {
                            Ext4.getCmp('institutions-grid-panel').getStore().load();
                            window.close();

                        }
                        else {
                            Ext4.MessageBox.alert('Something went wrong', 'Unable to save/update Institution');
                        }

                    }
                });

            }
        }
    ]
});

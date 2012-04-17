Sparrow.ux.DockPanel = Ext.extend(Ext.Panel, {
	splitterHeight: 10,
	constructor : function(config) {
		Sparrow.ux.DockPanel.superclass.constructor.call(this, {id: config.id});
		this.initialConfig = config;
		this.parentPanel = config.parentPanel;
		this.parentPanel.on('afterrender', this.attachDockContainer, this);
		this.parentPanel.on('resize', this.updateDockPositionOnParentResize, this);
	},
	
	attachDockContainer: function(parent) {
		var h = this.initialConfig.height || 1;
		var w = this.initialConfig.width || 1;
		
		var pw = parent.getWidth();
		var dock = document.createElement('div');
		dock.id = Ext.id(dock, "sparrow-ux"); 
		dock.style.position = "absolute";
		dock.style.left = (pw-w-this.splitterHeight) + "px";
		dock.style.top = (this.parentPanel.tbar.dom.clientHeight + this.splitterHeight) + "px";
		dock.style.height= h+"px";
		dock.style.width= w+"px";
		dock.style.backgroundColor = "transparent";
		dock.style.zIndex = "999";

		this.dockContainer = dock;
		parent.el.dom.appendChild(dock);
		
		this.dockPanel = new Ext.Panel({ 
			renderTo: this.dockContainer,
			title: '',
			layout: 'border',
			height: h,
			width: w,
			hidden: false,
			border: false,
			items: [
			{
				xtype: 'panel',
				region: 'east',
				title: this.initialConfig.title,
				layout: 'fit',
				border: this.initialConfig.border,
				frame: false,
				collapsible: true,
				split: false,
				collapsed: false,
				minSize: 325,
				width: w-this.splitterHeight,
				height: h,
				items: [this.initialConfig.contentPanel],
				listeners: {
					collapse: function(){
						if(this.dockPanel) {
							this.dockContainer.style.height= "1px";
							this.dockContainer.style.width= "1px";
						}
					},
					scope: this
				}
			},
			{	//should be invisible
				xtype: 'panel',
				region: 'center',
				border: false,
				minHeight: 0, 
				bodyStyle: 'background-color: transparent;',
				ctCls: 'sparrow-transparent'
			}
			]
		});
		
		this.dockPanel.render();
		this.splitter = this.dockPanel.body.dom.childNodes;
		for(var i = 0; i < this.splitter.length; i ++) {
			if(this.splitter[i].className.indexOf('x-layout-split') >= 0) {
				this.splitter = this.splitter[i];
				break;
			}
		}
		var _this = this;
		this.dockPanel.hide();
		this.dockPanel.items.items[0].collapse();
	},
	
	updateDockPositionOnParentResize : function(parent, aw, ah, rw, rh) {
		this.dockContainer.style.left = (parent.getWidth()-this.dockContainer.clientWidth-this.splitterHeight) + "px";
	},

	doExpand: function(animate) {
		var h = this.initialConfig.height;
		var w = this.initialConfig.width;
		this.dockContainer.style.height= h+"px";
		this.dockContainer.style.width= w+"px";
		this.updateDockPositionOnParentResize(this.parentPanel, this.parentPanel.getWidth(), this.parentPanel.getHeight());
		this.dockPanel.doLayout();
		this.dockPanel.show();
		this.dockPanel.items.items[0].expand();
		this.dockPanel.items.items[0].setWidth(w-this.splitterHeight); //5 padding is for the split bar which does resizing
	}
});
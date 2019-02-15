/**
 * From: http://extjs.com/forum/showthread.php?t=39161
 */

/*global Ext */

Ext.override(Ext.grid.GridView, {
	layout : function() {
		if (!this.mainBody) {
			return;
		}
		var g = this.grid;
		var c = g.getGridEl();
		var csize = c.getSize(true);
		var vw = csize.width;
		var vh;

		if (vw < 20 || csize.height < 20) {
			return;
		}

		var ltMax = true;
		if (this.maxHeight) {
			ltMax = csize.height < this.maxHeight;
		}

		if (g.autoHeight && ltMax) {
			this.scroller.dom.style.overflow = 'visible';
		} else {
			var height = csize.height;
			if (this.maxHeight) {
				if (height > this.maxHeight) {
					height = this.maxHeight;
					this.scroller.dom.style.overflow = '';
				}
			}

			this.el.setSize(csize.width, height);

			var hdHeight = this.mainHd.getHeight();
			vh = height - (hdHeight);

			this.scroller.setSize(vw, vh);
			if (this.innerHd) {
				this.innerHd.style.width = (vw) + 'px';
			}
		}
		if (this.forceFit) {
			if (this.lastViewWidth != vw) {
				this.fitColumns(false, false);
				this.lastViewWidth = vw;
			}
		} else {
			this.autoExpand();
			this.syncHeaderScroll();
		}
		this.onLayout(vw, vh);
	}
});

Ext.override(Ext.form.CheckboxGroup, {

	afterRender : function() {
		var that = this;
		this.items.each(function(i) {
			that.relayEvents(i, ['check', 'change']);
		});

		Ext.form.CheckboxGroup.superclass.afterRender.call(this);
	},

	getNames : function() {
		var n = [];

		this.items.each(function(item) {
			if (item.getValue()) {
				n.push(item.getName());
			}
		});

		return n;
	},

	getValues : function() {
		var v = [];

		this.items.each(function(item) {
			if (item.getValue()) {
				v.push(item.getRawValue());
			}
		});

		return v;
	},

	setValues : function(v) {
		var r = new RegExp('(' + v.join('|') + ')');

		this.items.each(function(item) {
			item.setValue(r.test(item.getRawValue()));
		});
	}
});

Ext.override(Ext.form.RadioGroup, {
	getName : function() {
		return this.items.first().getName();
	},

	getValue : function() {
		var v;

		this.items.each(function(item) {
			v = item.getRawValue();
			return !item.getValue();
		});

		return v;
	},

	setValue : function(v) {
		this.items.each(function(item) {
			item.setValue(item.getRawValue() == v);
		});
	}
});

Ext.override(Ext.form.Checkbox, {
    initCheckEvents : function() {
        this.innerWrap.removeAllListeners();
        this.innerWrap.addClassOnOver(this.overCls);
        this.innerWrap.addClassOnClick(this.mouseDownCls);
        this.innerWrap.on('click', this.onClick, this);
        this.innerWrap.on('keyup', this.onKeyUp, this);
        // Adding this event handler fixes the blur issue in EFD-565
        this.innerWrap.on('blur', this.onBlur, this);
    }
});

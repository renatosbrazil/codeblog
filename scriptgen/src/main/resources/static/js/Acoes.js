class Acoes {
	
	gerar(){
		this._loadingButton("gerar", 10);
		$("#idgerar").submit();
	}
	
	_loadingButton(divId, padd){
		$("#" + divId).attr("disabled", "disabled");
		$("#" + divId).html('<div style="padding-left:' + padd + 'px;padding-right:' + padd + 'px;"><span class="spinner-border spinner-border-sm" aria-hidden="true"></span></div>');
		$("#cancelar").attr("disabled", "disabled");
		$("#cancelarc").attr("disabled", "disabled");
	}
	
}
/**
 * @license Copyright (c) 2003-2018, CKSource - Frederico Knabben. All rights reserved.
 * For licensing, see https://ckeditor.com/legal/ckeditor-oss-license
 */

//  모바일 체크
if(typeof(G5_IS_MOBILE) == "undefined") g6_is_mobile = false;


CKEDITOR.editorConfig = function( config ) {
	g6_url = "plugin/";
	// 에디터 높이 설정
	if(typeof(editor_height) != "undefined") {
		config.height = editor_height+"px";
	}

	config.table_defaultWidth = '100%';
	config.table_defaultBorder = '1';
	config.table_defaultCellSpacing = '0';
	config.table_defaultCellPadding = '0';

    	// 원본 속성 허용
    	config.allowedContent = true;
    	config.extraAllowedContent = '*(*);*{*}'; 
    	// Word/웹에서 복사 시 스타일 보존
    	config.pasteFromWordRemoveStyles = false;
	config.pasteFromWordRemoveFontStyles = false;

	config.extraPlugins = (config.extraPlugins ? config.extraPlugins + ',timestamp' : 'timestamp');
	// 언어 설정
	config.language = 'ko';
        // 글꼴관련
        config.font_names = '맑은 고딕;굴림;굴림체;궁서;궁서체;돋움;돋움체;바탕;바탕체;';  // + CKEDITOR.config.font_names;
        config.font_defaultLabel = '맑은 고딕';
        //config.font_defaultLabel = 'Malgun Gothic';
        // 글자크기 출력
        config.fontSize_sizes = '8pt;9pt;10pt;11pt;12pt;14pt;16pt;20pt;24pt;30pt;48pt;60pt;72pt;';

        // 툴바 기능버튼 순서
        config.toolbarGroups = [
                { name: '1', groups: [ 'styles', 'align', 'basicstyles', 'cleanup', 'Timestamp' ] },
                { name: '2', groups: [ 'insertImg', 'insert', 'colors', 'list', 'blocks', 'links', 'mode', 'tools', 'about' ] }
        ];

	//config.removePlugins = 'showborders';
	//config.contentsCss = [ CKEDITOR.basePath + 'contents.css' ];
	config.contentsCss = [ CKEDITOR.basePath + 'contents.css?v=' + new Date().getTime() ];

	// 글꼴관련
	config.font_names = '맑은 고딕/Malgun Gothic;굴림/Gulim;돋움/Dotum;바탕/Batang;Arial/Arial;Tahoma/Tahoma;Verdana/Verdana;';
	config.fontSize_sizes = '8pt;9pt;10pt;11pt;12pt;14pt;16pt;20pt;24pt;30pt;48pt;60pt;72pt;';
	//config.font_names = '맑은 고딕;굴림;굴림체;궁서;궁서체;돋움;돋움체;바탕;바탕체;';  // + CKEDITOR.config.font_names;
	config.font_defaultLabel = '맑은 고딕';
	//config.font_defaultLabel = 'Malgun Gothic';
    	config.fontSize_defaultLabel = '12pt';
	// 글자크기 출력

	// 미노출 기능버튼
	if(g6_is_mobile) {
		//--- 모바일 ---//
		config.removeButtons = 'Print,Cut,Copy,Paste,Subscript,Superscript,Anchor,Unlink,ShowBlocks,Undo,Redo,Smiley,Font,Italic,Underline,Strike,BGColor';

        config.toolbarGroups = [
            { name: '1', groups: [ 'styles', 'align', 'basicstyles', 'insertImg', 'colors', 'links', 'mode' ] }
        ];
	} else {
		//--- PC ---//
		config.removeButtons = 'Print,Cut,Copy,Paste,Subscript,Superscript,Anchor,Unlink,ShowBlocks,Undo,Redo,Smiley';
	}

	/* 이미지 업로드 관련 소스 */
	let up_url = g6_url + "editor/ckeditor4/imageUpload/upload?type=Images";

	// 에디터 구분
	if(typeof(editor_id) != "undefined" && editor_id != "") {
		up_url += "&editor_id="+editor_id;
	}
	// 업로드 경로 - editor_uri
	if(typeof(editor_uri) != "undefined" && editor_uri != "") {
		up_url += "&editor_uri="+editor_uri;
	}
	// 업로드 이미지용 토큰
	if( typeof(editor_form_name) != "undefined" && editor_form_name != "") {
		up_url += "&editor_form_name="+editor_form_name;
	}
    
	// 업로드 페이지 URL 선언
	config.filebrowserImageUploadUrl = up_url;

	// 이미지 다이얼로그 수정 
	CKEDITOR.on('dialogDefinition', function (ev) {
		let dialogName = ev.data.name;
		let dialog = ev.data.definition.dialog;
		let dialogDefinition = ev.data.definition;
		if (dialogName == 'image') {
			dialog.on('show', function (obj) {
				//this.selectPage('Upload'); //업로드텝으로 시작
			});
			dialogDefinition.removeContents('advanced'); // 자세히탭 제거
			dialogDefinition.removeContents('Link'); // 링크탭 제거
			
			var infoTab = dialogDefinition.getContents('info');   
			infoTab.remove('txtHSpace');
			infoTab.remove('txtVSpace');
			infoTab.remove('htmlPreview');	// 미리보기 제거
		}
	});

	// 사용할 플러그인 추가
    let ck_extraPlugins = 'uploadwidget,uploadimage,editorplaceholder,list,timestamp';
    config.enterMode = CKEDITOR.ENTER_P;
    config.shiftEnterMode = CKEDITOR.ENTER_BR;

    config.extraPlugins = ck_extraPlugins;

	// 본문내용 불러들일때 속성유지
	config.allowedContent = true;

	// iOS만 적용
	if(/iPhone|iPad|iPod/i.test(navigator.userAgent) ) {
		// 한글 입력 관련 줄바꿈 과정에서 문제발생하여 적용
		config.removePlugins = 'enterkey';
	}

	if (localStorage.getItem('color-theme') == 'dark') {
		config.contentsCss = [CKEDITOR.getUrl('darkmode.css')];
    }
};
CKEDITOR.on('dialogDefinition', function(ev) {
    var dialogName = ev.data.name;
    var dialogDefinition = ev.data.definition;

    if (dialogName == 'table') {
        var infoTab = dialogDefinition.getContents('info');

        // 너비 기본값
        var widthField = infoTab.get('txtWidth');
        widthField['default'] = '90%';

	// 행 기본값
        var rowsField = infoTab.get('txtRows');
        rowsField['default'] = '4';

        // 열 기본값
        var colsField = infoTab.get('txtCols');
        colsField['default'] = '3';

        // 테두리 기본값
        var borderField = infoTab.get('txtBorder');
        borderField['default'] = '1';

        // 셀 간격
        var cellSpacing = infoTab.get('txtCellSpace');
        cellSpacing['default'] = '0';

        // 셀 여백
        var cellPadding = infoTab.get('txtCellPad');
        cellPadding['default'] = '5';

	// ★ 제목 행 기본 선택
        var headersField = infoTab.get('selHeaders');
        if (headersField) {
            headersField['default'] = 'row'; 
            // 값 옵션:
            // ''     : 제목 없음
            // 'row'  : 첫 행을 제목행
            // 'col'  : 첫 열을 제목열
            // 'both' : 첫 행+첫 열 모두 제목
        }
    }
});

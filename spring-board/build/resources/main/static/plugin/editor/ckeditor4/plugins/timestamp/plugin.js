CKEDITOR.plugins.add('timestamp', {
    icons: 'timestamp',
    init: function(editor) {
        editor.addCommand('insertTimestamp', {
            exec: function(editor) {
                const now = new Date();
                const timestamp =
                    now.getFullYear() + '-' +
                    ('0' + (now.getMonth() + 1)).slice(-2) + '-' +
                    ('0' + now.getDate()).slice(-2) + ' ' +
                    ('0' + now.getHours()).slice(-2) + ':' +
                    ('0' + now.getMinutes()).slice(-2);

                editor.insertHtml(timestamp);
            }
        });

        editor.ui.addButton('Timestamp', {
            label: 'Insert Timestamp',
            command: 'insertTimestamp',
            toolbar: 'insert'
        });
    }
});

var mingui = quixot.Mingui;
mingui.unfreeze();

var menuBar = mingui.addMenuBar('root', 'APP NAME');

mingui.addMenuItem('root', 'item', 'Refresh', 'JS_EXEC:window.location.href=window.location.href;');
mingui.addDropDownMenu('root', 'drop_down', 'MinGui Actions');
mingui.addMenuItem('drop_down', 'drop_down_toast', '.toast', 'JS_EXEC:toast();');
mingui.addMenuItem('drop_down', 'drop_down_notify', '.notify', 'JS_EXEC:notify();');
mingui.addSeparator('drop_down');

for(var i in quixot.Mingui.dialog.style){
    var style = quixot.Mingui.dialog.style[i];
    mingui.addMenuItem('drop_down', 'drop_down_dialog_' + i, 'DIALOG_' + i,
        'JS_EXEC:quixot.Mingui.dialog("'+style+'", "title' + style + '", "'+style +' some text", "CALLBACK:dialogOk", "CALLBACK:dialogNotOk");');
}


mingui.addMenuItem('drop_down', 'drop_down_item 2', 'MenuItem 2', 'ROUTE:20');


function toast(){
    mingui.toast('menu added');
}

function dialogOk(args){
    console.log('ok' + args);
}


function dialogNotOk(args){
    console.log('not ok' + args);
}


function notify(){
    mingui.notify('app name', 'started', 'JS_EXEC:alert("clicked");');
}


function changeFunc(args){
    console.log(args)
    console.log('HASH = ' + mingui.getHash('form', 'name'));
}


function buttonClick(id, text){
    console.log(id + text);
}



var page = mingui.addPage('page-id','horizontal');
console.log('page id=' + page);
mingui.showPage(page);


mingui.defineHash('form');
mingui.addInput('input-id-unique', page, 'text', 2, 20, true,
    'label text', '@icon', 'Init value', 'options', 'CALLBACK:changeFunc', 'name', 'form', 'clazz')


mingui.addButton('id-button', page, 'button-text', '@icon', 'CALLBACK:buttonClick');

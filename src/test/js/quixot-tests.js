if(typeof quixot === 'undefined'){
    var quixot = require('../dist/quixot.js');
}
console.log("running tests...")

var invalidDate =  new Date('Sun Dec 17 1995 43:24:00');
var validDate = new Date('Sun Dec 17 1995 03:24:00 GMT+0200 (EET)');
var sancho = quixot.Sancho.getInstance();

//globals
sancho.module('util');

sancho.submodule('object keys');
sancho.deepEquals(quixot.Util.keys({a:b}), ['a']);
sancho.deepEquals(quixot.Util.keys({a:b, b: function(){}, c: new Error()}), ['a', 'b', 'c']);

sancho.submodule('type check strings');

sancho.isTrue(quixot.Util.isString('file://test'));
sancho.isFalse(quixot.Util.isString(0));
sancho.isTrue(quixot.Util.isString(''));
sancho.isFalse(quixot.Util.isString(new Date()));
sancho.isFalse(quixot.Util.isString(true));
sancho.isFalse(quixot.Util.isString(false));
sancho.isFalse(quixot.Util.isString(null));
sancho.isFalse(quixot.Util.isString(Infinity));
sancho.isFalse(quixot.Util.isString(-Infinity));
sancho.isFalse(quixot.Util.isString(0 / 1000));

sancho.submodule('type check arrays');

sancho.isTrue(quixot.Util.isArray([1]));
sancho.isTrue(quixot.Util.isArray([1, '1']));
sancho.isTrue(quixot.Util.isArray([]));
sancho.isFalse(quixot.Util.isArray(1));
sancho.isFalse(quixot.Util.isArray(NaN));
sancho.isFalse(quixot.Util.isArray(Infinity));
sancho.isFalse(quixot.Util.isArray(null));
sancho.isFalse(quixot.Util.isArray({a: 1}));

sancho.submodule('type check numbers');
sancho.isFalse(quixot.Util.isNumber(NaN));
sancho.isTrue(quixot.Util.isNumber(Infinity));


sancho.submodule('lead');
sancho.equals(quixot.Util.lead(9, 2, 0), '09')
sancho.equals(quixot.Util.lead(9, 2, 'vvx'), 'vvx9')



sancho.submodule('stringify');

sancho.deepEquals(quixot.Util.stringify(null), 'null')
sancho.deepEquals(quixot.Util.stringify(true), 'true')
sancho.deepEquals(quixot.Util.stringify(false), 'false')
sancho.deepEquals(quixot.Util.stringify(0), '0');
sancho.deepEquals(quixot.Util.json.parse('{"a":1}'), {a:1})

var circular = {a: 1, b: false, c: 'str'};
circular.d = circular;
sancho.deepEquals(quixot.Util.stringify(circular), '{"a":1,"b":false,"c":"str","d":{"a":1,"b":false,"c":"str","d":{"a":1,"b":false,"c":"str","d":{"a":1,"b":false,"c":"str","d":{"a":1,"b":false,"c":"str","d":{"a":1,"b":false,"c":"str","d":{"a":1,"b":false,"c":"str","d":{"a":1,"b":false,"c":"str","d":{"a":1,"b":false,"c":"str","d":{[stack]:[stack],[stack]:[stack],[stack]:[stack],[stack]:[stack]}}}}}}}}}}')

sancho.deepEquals(quixot.Util.stringify(NaN), JSON.stringify(NaN));
sancho.deepEquals(quixot.Util.stringify(Infinity), JSON.stringify(Infinity));
sancho.deepEquals(quixot.Util.stringify(-Infinity), JSON.stringify(-Infinity));

var tests = [
    NaN,
    Infinity,
    -Infinity,
    [1, 2, 3, []],
    [{}, {}, {a: 1, b :[], c: 4}],
    {a:[], b: { c: 2, d: false, e: NaN, g: Infinity}},
    {bddbdbd: 'asdasdasd', fsfsdfsdfsdf: function(){}, ldsadsad: [function(){}, sancho.deepEquals, 1, false, -Infinity, 'fsdfsdfsdf']},
    {a: sancho.deepEquals},
    {validd: validDate},
    function (){},
    /a/g,
    new RegExp('a'),
    { r: new RegExp('a'), v: function(){}},
    [function (){quixot.Util.stringify('a');}, function (){quixot.Util.stringify('b');}, function (){quixot.Util.stringify('c');}]
];

for(var i = 0; i < tests.length; i++){
    var b = JSON.stringify(tests[i]);
    var a = quixot.Util.stringify(tests[i]);
    console.log(a, b);
    sancho.equals(a, b);
}

sancho.submodule('arrayDiff');
sancho.deepEquals(quixot.Util.arrayDiff([1, 2, 3], [2, 3]), [1]);
sancho.deepEquals(quixot.Util.arrayDiff([1, 2, '3'], [1, 2]), ['3']);

sancho.submodule('atos');
sancho.equals(quixot.Util.atos(0), 'a');
sancho.equals(quixot.Util.atos(Infinity), 'inf');
sancho.equals(quixot.Util.atos({a: Infinity}), 'dafnulle');
sancho.equals(quixot.Util.atos(''), 'a');
sancho.equals(quixot.Util.atos(validDate), 'ZraMtdaylaPxCVBjWcqcR');
sancho.equals(quixot.Util.atos(validDate, '!@#$%^&*()_{}:"?><+}'.split('')), '%<!^!?$!@%$!#%!%(%"!!!&!^?');
sancho.equals(quixot.Util.atos(validDate, ['a', 3, '4', 3, 6, 8,0, 'f', '_', '---------', 'tx', '565678', 'ggg', 'hhg']),
    'hhg0atx3f3a6tx8080f_a3gggtxtxtxtxtxtxtxtxtx3hhg_gggaa6txtxtxtxtxtxtxtxtxtxundefinedf8080f_');

sancho.equals(quixot.Util.atos(), 'undefined');
sancho.equals(quixot.Util.atos(null), 'null');
sancho.equals(quixot.Util.atos('a2'), 'ac');
sancho.equals(quixot.Util.atos([1, 2, 3]), 'abcccdb');
sancho.equals(quixot.Util.atos([]), 'ab');
sancho.equals(quixot.Util.atos([], 'xsssdddddddddddddddddd'.split('')), 'xs');
sancho.equals(quixot.Util.atos({}), 'de');
sancho.equals(quixot.Util.atos({a:1}), 'dafbe');
sancho.equals(quixot.Util.atos({a:1,c:[],e:[0,2,3,4,55,555556666]}), 'dafbccfabcefaacccdcecffcfffffggggbe');

sancho.submodule('aton');
sancho.equals(quixot.Util.aton('as'), 875);
sancho.equals(quixot.Util.aton(0), 0);
sancho.equals(quixot.Util.aton([]), 631);
sancho.equals(quixot.Util.aton([1,2,'x']), 842);
sancho.equals(quixot.Util.aton('sssssss'), 1468);
//        sancho.equals(quixot.Util.aton(Infinity), Infinity);
sancho.notEquals(quixot.Util.aton(NaN), quixot.Util.aton('NaN'));
sancho.notEquals(quixot.Util.aton(123), quixot.Util.aton('123'));
sancho.notEquals(quixot.Util.aton(null), quixot.Util.aton('null'));


sancho.submodule('booleanGetter');
sancho.equals(quixot.Util.booleanGetter('isRunning'), 'isRunning');
sancho.equals(quixot.Util.booleanGetter('running'), 'isRunning');
sancho.equals(quixot.Util.booleanGetter('isrunning'), 'isRunning');
sancho.equals(quixot.Util.booleanGetter('RUNNING'), 'isRunning');
sancho.equals(quixot.Util.booleanGetter('RUNNING'), 'isRunning');
sancho.equals(quixot.Util.booleanGetter('ISRUNNING'), 'isRUNNING');
sancho.equals(quixot.Util.booleanGetter('ISA'), 'isA');
sancho.equals(quixot.Util.booleanGetter('iSA'), 'isA');
sancho.equals(quixot.Util.booleanGetter('Isa'), 'isA');
sancho.equals(quixot.Util.booleanGetter('isa'), 'isA');
sancho.equals(quixot.Util.varGetter('isa'), 'getIsa');
sancho.equals(quixot.Util.varGetter('isaA'), 'getIsaA');


sancho.submodule('randStr');
for(var i = 0; i < 10; i++){
    sancho.notEquals(quixot.Util.randStr(), quixot.Util.randStr());
}


sancho.submodule('lead');
sancho.equals(quixot.Util.lead(0, 2, '0'), '00');
sancho.equals(quixot.Util.lead(123, 4, '-'), '-123');
sancho.equals(quixot.Util.lead(123, 2, '-'), '123');
sancho.equals(quixot.Util.lead(12345, Infinity, '-'), '12345');
sancho.equals(quixot.Util.lead(Infinity, 'Infinity'.length + 1, '0'), '0Infinity');

sancho.equals(quixot.URL.decode('file://test').protocol, 'file');



sancho.deepEquals(quixot.URL.normalizeArg('a'), 'a')
sancho.deepEquals(quixot.URL.normalizeArg('1'), 1)
sancho.deepEquals(quixot.URL.normalizeArg(2), 2);
sancho.deepEquals(quixot.URL.normalizeArg(2), 2);
sancho.deepEquals(quixot.URL.normalizeArg('1,2,3'), [1,2,3]);
sancho.deepEquals(quixot.URL.normalizeArg(), 'undefined');

sancho.deepEquals(quixot.URL.normalizeArg('false'), 'false');
sancho.isFalse(quixot.URL.normalizeArg(false));
sancho.isTrue(quixot.URL.normalizeArg(true));
sancho.isTrue(quixot.URL.normalizeArg('true'));



sancho.equals(quixot.URL.decode('file://test').protocol, 'file');
sancho.equals(quixot.URL.decode('file://test').lastPage, 'test');
sancho.equals(quixot.URL.decode('file://test/wat/rest?23').lastPage, 'rest');
sancho.equals(quixot.URL.decode('file://test/wat/rest?23').parts[1], 'wat');
sancho.equals(quixot.URL.decode('file://test').parts[0], 'test');
sancho.equals(quixot.URL.decode('file://test?unu=1').params.unu, 1);
sancho.equals(quixot.URL.decode('file://test?doi=2&trei=1,2,3').params.trei[1], 2);
sancho.equals(quixot.URL.decode('file://test?doi=2&trei=1,2,3').lastPage, 'test');

sancho.equals(quixot.URL.decode('\n\tdoi=2&trei=1,2,3').params.doi, 2);
sancho.equals(quixot.URL.decode('\n\t  file://test24    ').url, 'test24');
sancho.equals(quixot.URL.decode('?doi=fff&trei=1,2,3').params.doi, 'fff');

var image = new quixot.GOG.Image('type=button&src=some/source&click=action();&hover=other/src/path.png');

sancho.isTrue(image.isButton, 'is button');
sancho.equals(image.src, 'some/source');
sancho.equals(image.hover, 'other/src/path.png');
sancho.equals(image.click, 'action();');

//console.log(image);





sancho.equals(quixot.URL.parseGETArg('unu=1&2=doi').unu, 1);
sancho.equals(quixot.URL.parseGETArg('unu=\'1\'&2=doi').unu, '\'1\'');


sancho.module('memo');
quixot.dropMemo();
sancho.isTrue(quixot.Util.isFullUpper('ABC'));
sancho.equals(quixot.memo().c.ABC, true);
sancho.isFalse(quixot.Util.isFullUpper('ssT'));
sancho.equals(quixot.memo().c.ssT, false);



if(typeof quixot === 'undefined'){

        quixot = require('./dist/quixot.js');


}


var a = [quixot.Util.randNr(), quixot.Util.randNr(),
    quixot.Util.randNr(), quixot.Util.randNr(),
    quixot.Util.randNr(), quixot.Util.randNr(),
    quixot.Util.randNr(), quixot.Util.randNr(),
    quixot.Util.randNr(), quixot.Util.randNr(),
    quixot.Util.randNr(), quixot.Util.randNr(),
    quixot.Util.randNr(), quixot.Util.randNr(),
    quixot.Util.randNr(), quixot.Util.randNr(),
    quixot.Util.randNr(), quixot.Util.randNr()];

quixot.Sancho.noDuplicates(a);


quixot.Sancho.donkey().burden(function (e) {
    console.log(e, 'running');
}).freeVal('console.log({string}, {number}, {number} + {number})', 4).run();

var Sancho = quixot.Sancho, eq = Sancho.equals, time = quixot.Time, dulcineea = quixot.Dulcineea;



eq(time.interval(1, 'nano'), 1);
eq(time.interval(2, 'seconds'), 2 * 1000);
eq(time.interval(4, 'second'), 4 * 1000);
eq(time.interval(3, 'minute'), 3 * 60 * 1000);
eq(time.interval(1, 'minutes'), 1 * 60 * 1000);
eq(time.interval(2, 'hour'), 2 * 60 * 60 * 1000);
eq(time.interval(2, 'day'), 2 * 24 * 60 * 60 * 1000);
eq(time.interval(3, 'month'), 3 * 30 * 24 * 60 * 60 * 1000);
eq(time.interval(1, 'year'), 1 * 365 * 24 * 60 * 60 * 1000);
eq(quixot.Util.aton('a'), '1');
eq(quixot.Util.aton(3123213213+'a'), '31232132131');
eq(quixot.Util.aton(1234+'bcd'), '1234234');
eq(
    quixot.Util.aton('abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ'),
    "12345678910111213141516171819202122232425262728293031323334353637383940414243444546474849505152"
);

eq(
   quixot.Util.aton('\'";:,<.>/?[{]}=+-_)(*&^%$#@!`~\t\n '),
   "535455565758596061626364656667686970717273747576777879808182838485"
);

eq (
    quixot.Util.aton('abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ\'";:,<.>/?[{]}=+-_)(*&^%$#@!`~\t\n '),
    "12345678910111213141516171819202122232425262728293031323334353637383940414243444546474849505152535455565758596061626364656667686970717273747576777879808182838485"
);

eq(
    quixot.Util.aton('\""', '__'),
    "54__54"
)


eq(quixot.URL.getDomainFromUrl('https://www.mydomain.com/page?args=more'), "www.mydomain.com");

eq(
    quixot.URL.querify({a:1, b:[1, 2, 3], g:"text", c:{d:2, f:"some string"}}),
    'a=1&b=[1,2,3]&g=text&c={"d":2,"f":"some string"}'
)

eq (
    quixot.URL.decode('http://mydomain/page1/page2/finalPage?arg0=1,2,3&arg1=[1,2,3]').lastPage,
    'finalPage'
);


eq (
    quixot.URL.decode('protocol_test://mydomain/page1/page2/finalPage?arg0=1,2,3&arg1=[1,2,3]').protocol,
    'protocol_test'
);

eq (
    quixot.URL.decode('protocol_test://mydomain/page1/page2/finalPage?arg0=1,2,3&arg1=[1,2,3]').parts[2],
    'page2'
);

eq (
    quixot.URL.decode('protocol_test://mydomain/page1/page2/finalPage?arg0=1,2,3&arg1=[1,2,3]').params.arg1[0],
    '[1'
);

eq (
    quixot.URL.decode('protocol_test://mydomain/page1/page2/finalPage?arg0=1,2,3&arg1=[1,2,3]').params.arg0[0],
    '1'
);

Sancho.deepEquals(dulcineea.compiler.extract('a.b.c'), ['a', 'b', 'c']);
Sancho.deepEquals(dulcineea.compiler.extract('a[0]c'), ['a', '0', 'c']);
Sancho.deepEquals(dulcineea.compiler.extract('a.b.c["prop"]d[23][\'data\']'), ['a', 'b', 'c', '"prop"', 'd', '23', '\'data\'']);

eq(
    quixot.Cookie.setc('test-cookie', 'test-cookie-value'),
    'test-cookie=test-cookie-value'
)

eq (
    quixot.Cookie.setc('test-cookie', 'test-cookie-value', null, 'path', 'domain', true),
    "test-cookie=test-cookie-value; path=path; domain=domain; secure"
)

var date = new Date();
eq (
    quixot.Cookie.setc('test-cookie', 'test-cookie-value', date, 'path', 'domain', true),
    "test-cookie=test-cookie-value; expires=" + quixot.Time.dateToString(date, 'cookie') + "; path=path; domain=domain; secure"
)

eq(
    dulcineea.compiler.execute('a', {a:1}),
    1
)
eq(
    dulcineea.compiler.execute('a.b', {a:{b:'value'}}),
    'value'
)

eq(
    dulcineea.compiler.execute('a[0]b', {a:[{b:'item_in_list'}]}),
    'item_in_list'
)

eq(
    dulcineea.compiler.execute('a[0]b', {a:[{b:'item_in_list'}]}),
    'item_in_list'
)

eq(
    dulcineea.compiler.execute('a[0]b["prop"][4]', {a:[{b:{prop:[0, 9, 4, 3, 5]}}]}),
    5
)

Sancho.hasProperty(quixot.Event.getAll(), 'events');
Sancho.hasProperty(quixot.Event.getAll(), 'dispatchers');



var myLogger = quixot.Logger.getInstance('myLogger', false, function (data) {
             console.log('my logger custom callback');
});

if(typeof navigator != 'undefined') {
    myLogger.log('navigator-agent', navigator.userAgent);
}

myLogger.log('info', 'info level log');
eq(myLogger.getLogs().info.length, 1);
myLogger.log('custom_level', 'some custom log');
eq(myLogger.getLogs().info.length, 1)
eq(myLogger.getLogs().custom_level.length, 1)
myLogger.warn('custom warn message');
eq(myLogger.getLogs().warn.length, 1);
myLogger.error('error message');
eq(myLogger.getLogs().error.length, 1);

myLogger.info('quixot.Browser.name = ' + quixot.Browser.name);
myLogger.info('quixot.Browser.version = ' + quixot.Browser.version);
myLogger.info('quixot.System.os.name = ' + quixot.System.os.name);
myLogger.info('quixot.System.os.version = ' + quixot.System.os.version);

for(var i in quixot.Env){
    myLogger.info('quixot.Env.'+i+'=' + JSON.stringify(quixot.Env[i]));
}

for(var i in quixot.Browser.is){
    myLogger.info('quixot.Browser.is.'+i+'=' + JSON.stringify(quixot.Browser.is[i]));
}

quixot.Logger.setURLAccessKey('mySecretKey');

var log4CustomerSupport = quixot.Logger.getInstance('log4CustomerSupport', {consoleAppender: false, fileAppender: false});

log4CustomerSupport.trace('this log happens client side');
log4CustomerSupport.trace('and client could see stored data');
log4CustomerSupport.trace('by calling in console');
log4CustomerSupport.trace('quixot.Logger.getInstance(\'log4CustomerSupport\').getLogs().trace');

quixot.Util.atos(5, '123');
eq(quixot.Util.atos('aa'), 'aa');
eq(quixot.Util.atos(123), 'md');
eq(quixot.Util.atos(123456789), 'mdefghij');
eq(quixot.Util.atos({}), '[object Object]');
eq(quixot.Util.atos(0000001), 'b');
eq(quixot.Util.atos('0000001'), 'abcdefb');
eq(quixot.Util.atos('000000'), 'abcdef');
quixot.Cache.put("key", 'some_value')
eq(quixot.Cache.getSafe("key", null), 'some_value');

quixot.Cache.remove('key');

eq(quixot.Cache.getSafe("key", 'does_not_exist_anymore'), 'does_not_exist_anymore');
quixot.Sancho.donkey('console.log({any} + \"any\");');
//var a = quixot.Util.randStr(')#($@');

//Sancho.hasData(quixot.Env.javaPath);
Sancho.hasData(quixot.System.os.name);
Sancho.hasData(quixot.System.os.version);

var img = 'https://scontent-frt3-1.xx.fbcdn.net/v/t1.0-0/p118x118/15380632_604271709773684_2169990880710390381_n.jpg?oh=0c50a6c045ecded702c53527bcb7e14e&oe=58B6E4A7';

var testImages = [
    'https://raw.githubusercontent.com/alex2stf/quixot-js/master/img/salvador-dali-1.jpg',
    'https://raw.githubusercontent.com/alex2stf/quixot-js/master/img/don-quixote-2.jpg',
    'https://raw.githubusercontent.com/alex2stf/quixot-js/master/img/salvador-dali-2.jpg',
    'https://raw.githubusercontent.com/alex2stf/quixot-js/master/img/don-quixote-1.jpg',
    'https://raw.githubusercontent.com/alex2stf/quixot-js/master/img/salvador-dali-3.jpg'
];

for(var i = 0; i < testImages.length; i++){
    var title = 'Test notify (' + i + ')',
        intval = ((i+1) * (i + 1)),
        lifetime =  intval * 1000,
        text = ' This notification has a lifetime of ' + intval + ' seconds',
        notification =  quixot.Mingui.notify(title, text, testImages[i], lifetime);
}

function showMedia(p){
    _h();
    $('#nvcnt').css('background-color', '#c5c5c5');
    $('.nbt').css('background-color', '#c5c5c5');
    $('.nbt').css('border', 'none');
    $('.dvth').css('background-color', '#284660');
    $('body').css('background-color', colors[p + 'Dark']);
    $('#mdsrc-' + p).css('background-color', colors[p + 'Light'])
    $('#' + p + '-tab').css('background-color', colors[p + 'Dark']);
    $('.nvbar').css('background-color', colors[p + 'Dark']);
    $('#media-page').show();

    var mRoot = document.getElementById('media-list-' + p);
    if (!mRoot){
        var style = "background-color:" + colors[p+ 'Light'];
        mRoot = document.createElement('div');
        mRoot.setAttribute('class', 'media-list page');
        mRoot.id = 'media-list-' + p;

        mRoot.innerHTML = '<div id="nv-'+p+'" class="mnv">' +
            '<input type="text" id="mdsrc-' + p + '" onchange="mediaSearch(\''+p+'\')" style="'+style+'"></input>' +
            '<button id="plv-btn-'+p+'" onclick="playlistView()">' +
            '<img src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAEAAAABACAYAAACqaXHeAAAACXBIWXMAAAsTAAALEwEAmpwYAAAKT2lDQ1BQaG90b3Nob3AgSUNDIHByb2ZpbGUAAHjanVNnVFPpFj333vRCS4iAlEtvUhUIIFJCi4AUkSYqIQkQSoghodkVUcERRUUEG8igiAOOjoCMFVEsDIoK2AfkIaKOg6OIisr74Xuja9a89+bN/rXXPues852zzwfACAyWSDNRNYAMqUIeEeCDx8TG4eQuQIEKJHAAEAizZCFz/SMBAPh+PDwrIsAHvgABeNMLCADATZvAMByH/w/qQplcAYCEAcB0kThLCIAUAEB6jkKmAEBGAYCdmCZTAKAEAGDLY2LjAFAtAGAnf+bTAICd+Jl7AQBblCEVAaCRACATZYhEAGg7AKzPVopFAFgwABRmS8Q5ANgtADBJV2ZIALC3AMDOEAuyAAgMADBRiIUpAAR7AGDIIyN4AISZABRG8lc88SuuEOcqAAB4mbI8uSQ5RYFbCC1xB1dXLh4ozkkXKxQ2YQJhmkAuwnmZGTKBNA/g88wAAKCRFRHgg/P9eM4Ors7ONo62Dl8t6r8G/yJiYuP+5c+rcEAAAOF0ftH+LC+zGoA7BoBt/qIl7gRoXgugdfeLZrIPQLUAoOnaV/Nw+H48PEWhkLnZ2eXk5NhKxEJbYcpXff5nwl/AV/1s+X48/Pf14L7iJIEyXYFHBPjgwsz0TKUcz5IJhGLc5o9H/LcL//wd0yLESWK5WCoU41EScY5EmozzMqUiiUKSKcUl0v9k4t8s+wM+3zUAsGo+AXuRLahdYwP2SycQWHTA4vcAAPK7b8HUKAgDgGiD4c93/+8//UegJQCAZkmScQAAXkQkLlTKsz/HCAAARKCBKrBBG/TBGCzABhzBBdzBC/xgNoRCJMTCQhBCCmSAHHJgKayCQiiGzbAdKmAv1EAdNMBRaIaTcA4uwlW4Dj1wD/phCJ7BKLyBCQRByAgTYSHaiAFiilgjjggXmYX4IcFIBBKLJCDJiBRRIkuRNUgxUopUIFVIHfI9cgI5h1xGupE7yAAygvyGvEcxlIGyUT3UDLVDuag3GoRGogvQZHQxmo8WoJvQcrQaPYw2oefQq2gP2o8+Q8cwwOgYBzPEbDAuxsNCsTgsCZNjy7EirAyrxhqwVqwDu4n1Y8+xdwQSgUXACTYEd0IgYR5BSFhMWE7YSKggHCQ0EdoJNwkDhFHCJyKTqEu0JroR+cQYYjIxh1hILCPWEo8TLxB7iEPENyQSiUMyJ7mQAkmxpFTSEtJG0m5SI+ksqZs0SBojk8naZGuyBzmULCAryIXkneTD5DPkG+Qh8lsKnWJAcaT4U+IoUspqShnlEOU05QZlmDJBVaOaUt2ooVQRNY9aQq2htlKvUYeoEzR1mjnNgxZJS6WtopXTGmgXaPdpr+h0uhHdlR5Ol9BX0svpR+iX6AP0dwwNhhWDx4hnKBmbGAcYZxl3GK+YTKYZ04sZx1QwNzHrmOeZD5lvVVgqtip8FZHKCpVKlSaVGyovVKmqpqreqgtV81XLVI+pXlN9rkZVM1PjqQnUlqtVqp1Q61MbU2epO6iHqmeob1Q/pH5Z/YkGWcNMw09DpFGgsV/jvMYgC2MZs3gsIWsNq4Z1gTXEJrHN2Xx2KruY/R27iz2qqaE5QzNKM1ezUvOUZj8H45hx+Jx0TgnnKKeX836K3hTvKeIpG6Y0TLkxZVxrqpaXllirSKtRq0frvTau7aedpr1Fu1n7gQ5Bx0onXCdHZ4/OBZ3nU9lT3acKpxZNPTr1ri6qa6UbobtEd79up+6Ynr5egJ5Mb6feeb3n+hx9L/1U/W36p/VHDFgGswwkBtsMzhg8xTVxbzwdL8fb8VFDXcNAQ6VhlWGX4YSRudE8o9VGjUYPjGnGXOMk423GbcajJgYmISZLTepN7ppSTbmmKaY7TDtMx83MzaLN1pk1mz0x1zLnm+eb15vft2BaeFostqi2uGVJsuRaplnutrxuhVo5WaVYVVpds0atna0l1rutu6cRp7lOk06rntZnw7Dxtsm2qbcZsOXYBtuutm22fWFnYhdnt8Wuw+6TvZN9un2N/T0HDYfZDqsdWh1+c7RyFDpWOt6azpzuP33F9JbpL2dYzxDP2DPjthPLKcRpnVOb00dnF2e5c4PziIuJS4LLLpc+Lpsbxt3IveRKdPVxXeF60vWdm7Obwu2o26/uNu5p7ofcn8w0nymeWTNz0MPIQ+BR5dE/C5+VMGvfrH5PQ0+BZ7XnIy9jL5FXrdewt6V3qvdh7xc+9j5yn+M+4zw33jLeWV/MN8C3yLfLT8Nvnl+F30N/I/9k/3r/0QCngCUBZwOJgUGBWwL7+Hp8Ib+OPzrbZfay2e1BjKC5QRVBj4KtguXBrSFoyOyQrSH355jOkc5pDoVQfujW0Adh5mGLw34MJ4WHhVeGP45wiFga0TGXNXfR3ENz30T6RJZE3ptnMU85ry1KNSo+qi5qPNo3ujS6P8YuZlnM1VidWElsSxw5LiquNm5svt/87fOH4p3iC+N7F5gvyF1weaHOwvSFpxapLhIsOpZATIhOOJTwQRAqqBaMJfITdyWOCnnCHcJnIi/RNtGI2ENcKh5O8kgqTXqS7JG8NXkkxTOlLOW5hCepkLxMDUzdmzqeFpp2IG0yPTq9MYOSkZBxQqohTZO2Z+pn5mZ2y6xlhbL+xW6Lty8elQfJa7OQrAVZLQq2QqboVFoo1yoHsmdlV2a/zYnKOZarnivN7cyzytuQN5zvn//tEsIS4ZK2pYZLVy0dWOa9rGo5sjxxedsK4xUFK4ZWBqw8uIq2Km3VT6vtV5eufr0mek1rgV7ByoLBtQFr6wtVCuWFfevc1+1dT1gvWd+1YfqGnRs+FYmKrhTbF5cVf9go3HjlG4dvyr+Z3JS0qavEuWTPZtJm6ebeLZ5bDpaql+aXDm4N2dq0Dd9WtO319kXbL5fNKNu7g7ZDuaO/PLi8ZafJzs07P1SkVPRU+lQ27tLdtWHX+G7R7ht7vPY07NXbW7z3/T7JvttVAVVN1WbVZftJ+7P3P66Jqun4lvttXa1ObXHtxwPSA/0HIw6217nU1R3SPVRSj9Yr60cOxx++/p3vdy0NNg1VjZzG4iNwRHnk6fcJ3/ceDTradox7rOEH0x92HWcdL2pCmvKaRptTmvtbYlu6T8w+0dbq3nr8R9sfD5w0PFl5SvNUyWna6YLTk2fyz4ydlZ19fi753GDborZ752PO32oPb++6EHTh0kX/i+c7vDvOXPK4dPKy2+UTV7hXmq86X23qdOo8/pPTT8e7nLuarrlca7nuer21e2b36RueN87d9L158Rb/1tWeOT3dvfN6b/fF9/XfFt1+cif9zsu72Xcn7q28T7xf9EDtQdlD3YfVP1v+3Njv3H9qwHeg89HcR/cGhYPP/pH1jw9DBY+Zj8uGDYbrnjg+OTniP3L96fynQ89kzyaeF/6i/suuFxYvfvjV69fO0ZjRoZfyl5O/bXyl/erA6xmv28bCxh6+yXgzMV70VvvtwXfcdx3vo98PT+R8IH8o/2j5sfVT0Kf7kxmTk/8EA5jz/GMzLdsAAAAgY0hSTQAAeiUAAICDAAD5/wAAgOkAAHUwAADqYAAAOpgAABdvkl/FRgAAAvdJREFUeNrs202oVVUYBuDnHLc386IZ+JNCliYoSE0SB1oDSVCJZoJRg0gFFRypKEqJEAgOFARRJzpo0rRwYKQDJYjLjVRuBWnhVVC8agX+gZa35WDvCzs5P3cN9zr7Ha2911qD7z3nW9/PencjhPAZPtca32A9HkoUjRDCHczosGY5fig9z8SrFbf7bwxDhv4ui18ojV/HSayoOAFXsQlnmwhdFpfnDyRgPMzHEUxvRmyagfcTcv+FeDuGgJfQl9gZ2B9DwAjuJmT8Y4zEEPCw8JuQCAHfYjBDNxIapfHBwg02Pve+ajiP7XjaCCEM4c0Of5Ol+DnVRCjDDmxpM/8dfpEwGiEEvYymHkdNQE1ATUBvI8PLRXXUCrdxI/UweBKftpm/iNW4kzIBjzC5w5oVOFd6XiLvElUZV3B6zAW65fTl+eX4soPLVAUPsBPHm/ivy+JQOjD3J2A8TME+zIuJArOLwigVzMLiGAImVrwEboUJMQTckndTU8E9DDcjDsEn+KLYWHU8xQkMZZgQsfErjGJdxbPIARwaywPOYGWbhSNF6LsqUTRCCAuLbK8VLsn7Z0mnwnU1WBNQE1ATUBPQq8iwCKt6OQyexXtt5m9jWcqJUIZ3upSMrz1HwEdYW/HKcFB+0ftPVuT248XHOIqpFf/hP5ArXrbFaIT6sDsB4xUF4Aa8FRMF5mBBQu4/VWRL7F/pqEPGMBqrEfoxIeNH8GvT+CUyo/JW8u8JGH9f3hUezsQJJQfk3aB3Kx4GL8tFUhohhBNyQXQrXMAaiV+NTcMbHfzkZuqpcF0N1gTUBNQE1AT0KjJ5N2hzm/kzOJZgDfC/MNjzYulOJe4kTC8992EvPql4Kvw9tuFWZvwSGUUxtEf1hRIf4kWsjTkE++WfmqWiElmFpbEaoVkJuf8kvBJDwH15VyglPIoh4C5OJWT8b/gpRiMUsKvIDaqOP7AVf2boJpV9Uhpfl98NzK04AX/h2lgecLiI7a3wNYZauEIyH1A+GwB27LDDPN6zzwAAAABJRU5ErkJggg==">' +
            '</button>' +
            '</div>' +
            '<div id="media-content-'+p+'"></div>';
        $('#media-page').append(mRoot);
    }
    mRoot.style.display = 'block';

    fetchMedia(p);
    AppSettings.set("page", 'media-' + p);
}

function showSettingsView() {
    //#1b4825
    _h();
    $('body').css('background', '#1b4825');
    $('.nvbar').css('background', '#1b4825');
    $('.nbt').css('background', '#c5c5c5');
    $('.nbt').css('border', 'none');
    $('#nvcnt').css('background', '#c5c5c5');
    $('#settings-tab').css('background', '#1b4825');
    $('#sting').show();
    AppSettings.set("page", 'settingsView');
}

function showTab(tab){
    switch (tab){
        case 'music':
        case 'videos':
        case 'streams':
            showMedia(tab);
            break;
        case 'settings':
            showSettingsView();
            break;

    }
}
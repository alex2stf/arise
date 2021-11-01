var g_friends = {};


function extract_id(ip) {
    return ip.replace(/\./g, '').replace(/\:/g, '').replace(/\//g, '').replace(/http/g, 'x')
}


function recursive_test(arr, i){
    if(i > arr.length - 1){
        return;
    }
    var rhost = arr[i];

    ping_host(rhost, function (rh) {
        console.log('ok and alive ' + rh);
        place_ip('ips-container',arr[i], 'panel');
        recursive_test(arr, i+1);


    }, function () {
        console.log('ignore ' + rhost + ' because does not respond')
    })
}

function read_ips_from_cache(){
    $get("/props/get?key=friend-ips", function (data) {

        if(!data){
            return;
        }
        console.log(data);
        if(data.length > 0){
            recursive_test(data, 0);
        }
    })
}

read_ips_from_cache();

function place_friend_ips_for_air_play(divid, path, mode){
    for(var i in g_friends){
        console.log(divid, i, mode, path);
        place_ip(divid, i, mode, path);
    }
}





function place_ip(divId, rHost, mode, path) {
    var div = _g(divId);
    var ipid = extract_id(rHost);
    var nodeId = ipid + divId;
    var hname = rHost;
    if(g_friends[rHost]){
        hname = g_friends[rHost].name;
    }

    console.log(hname)

    var item = _g(nodeId);
    if(!item){
        item = document.createElement('div');
        item.id = nodeId;
        item.setAttribute('class', 'hpanel-cnt');
        div.append(item);
    }
    var airAction = '';
    if('air-play-send' === mode) {
        hname = getFileNameFromStr(decodeString(path)) + ' @' + hname;
        airAction = 'air_play(\'' + encodeURI(rHost) + '\', \'' + path + '\')';
    }

    if('air-url-open' == mode){
        hname = (decodeString(path)) + ' @' + hname;
        airAction = 'open_to_remote(\''+encodeURI(rHost)+'\', \''+encodeURI(path)+'\')';
    }
    var t = '<span class="host-name">'+hname+'</span>';

    if('air-play-send' === mode || 'air-url-open' == mode) {

        t += '<button class="host-btn" onclick="' + airAction + '">';
        t += '<img src="data:image/png;base64,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"/>'
        t += '</button>';
    }
    if('air-play-send' === mode){
        t+= '<button onclick="'+'air_play_distribute(\''+encodeURI(path)+'\')'+'">'
        t+='<img src="data:image/png;base64,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"/>';
        t+= '</button>';
    }

    if('panel' === mode){
        t+='<button onclick="remoteControl(\''+rHost+'\')">'
        t+='<img src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAEAAAABACAYAAACqaXHeAAAACXBIWXMAAAsTAAALEwEAmpwYAAAKT2lDQ1BQaG90b3Nob3AgSUNDIHByb2ZpbGUAAHjanVNnVFPpFj333vRCS4iAlEtvUhUIIFJCi4AUkSYqIQkQSoghodkVUcERRUUEG8igiAOOjoCMFVEsDIoK2AfkIaKOg6OIisr74Xuja9a89+bN/rXXPues852zzwfACAyWSDNRNYAMqUIeEeCDx8TG4eQuQIEKJHAAEAizZCFz/SMBAPh+PDwrIsAHvgABeNMLCADATZvAMByH/w/qQplcAYCEAcB0kThLCIAUAEB6jkKmAEBGAYCdmCZTAKAEAGDLY2LjAFAtAGAnf+bTAICd+Jl7AQBblCEVAaCRACATZYhEAGg7AKzPVopFAFgwABRmS8Q5ANgtADBJV2ZIALC3AMDOEAuyAAgMADBRiIUpAAR7AGDIIyN4AISZABRG8lc88SuuEOcqAAB4mbI8uSQ5RYFbCC1xB1dXLh4ozkkXKxQ2YQJhmkAuwnmZGTKBNA/g88wAAKCRFRHgg/P9eM4Ors7ONo62Dl8t6r8G/yJiYuP+5c+rcEAAAOF0ftH+LC+zGoA7BoBt/qIl7gRoXgugdfeLZrIPQLUAoOnaV/Nw+H48PEWhkLnZ2eXk5NhKxEJbYcpXff5nwl/AV/1s+X48/Pf14L7iJIEyXYFHBPjgwsz0TKUcz5IJhGLc5o9H/LcL//wd0yLESWK5WCoU41EScY5EmozzMqUiiUKSKcUl0v9k4t8s+wM+3zUAsGo+AXuRLahdYwP2SycQWHTA4vcAAPK7b8HUKAgDgGiD4c93/+8//UegJQCAZkmScQAAXkQkLlTKsz/HCAAARKCBKrBBG/TBGCzABhzBBdzBC/xgNoRCJMTCQhBCCmSAHHJgKayCQiiGzbAdKmAv1EAdNMBRaIaTcA4uwlW4Dj1wD/phCJ7BKLyBCQRByAgTYSHaiAFiilgjjggXmYX4IcFIBBKLJCDJiBRRIkuRNUgxUopUIFVIHfI9cgI5h1xGupE7yAAygvyGvEcxlIGyUT3UDLVDuag3GoRGogvQZHQxmo8WoJvQcrQaPYw2oefQq2gP2o8+Q8cwwOgYBzPEbDAuxsNCsTgsCZNjy7EirAyrxhqwVqwDu4n1Y8+xdwQSgUXACTYEd0IgYR5BSFhMWE7YSKggHCQ0EdoJNwkDhFHCJyKTqEu0JroR+cQYYjIxh1hILCPWEo8TLxB7iEPENyQSiUMyJ7mQAkmxpFTSEtJG0m5SI+ksqZs0SBojk8naZGuyBzmULCAryIXkneTD5DPkG+Qh8lsKnWJAcaT4U+IoUspqShnlEOU05QZlmDJBVaOaUt2ooVQRNY9aQq2htlKvUYeoEzR1mjnNgxZJS6WtopXTGmgXaPdpr+h0uhHdlR5Ol9BX0svpR+iX6AP0dwwNhhWDx4hnKBmbGAcYZxl3GK+YTKYZ04sZx1QwNzHrmOeZD5lvVVgqtip8FZHKCpVKlSaVGyovVKmqpqreqgtV81XLVI+pXlN9rkZVM1PjqQnUlqtVqp1Q61MbU2epO6iHqmeob1Q/pH5Z/YkGWcNMw09DpFGgsV/jvMYgC2MZs3gsIWsNq4Z1gTXEJrHN2Xx2KruY/R27iz2qqaE5QzNKM1ezUvOUZj8H45hx+Jx0TgnnKKeX836K3hTvKeIpG6Y0TLkxZVxrqpaXllirSKtRq0frvTau7aedpr1Fu1n7gQ5Bx0onXCdHZ4/OBZ3nU9lT3acKpxZNPTr1ri6qa6UbobtEd79up+6Ynr5egJ5Mb6feeb3n+hx9L/1U/W36p/VHDFgGswwkBtsMzhg8xTVxbzwdL8fb8VFDXcNAQ6VhlWGX4YSRudE8o9VGjUYPjGnGXOMk423GbcajJgYmISZLTepN7ppSTbmmKaY7TDtMx83MzaLN1pk1mz0x1zLnm+eb15vft2BaeFostqi2uGVJsuRaplnutrxuhVo5WaVYVVpds0atna0l1rutu6cRp7lOk06rntZnw7Dxtsm2qbcZsOXYBtuutm22fWFnYhdnt8Wuw+6TvZN9un2N/T0HDYfZDqsdWh1+c7RyFDpWOt6azpzuP33F9JbpL2dYzxDP2DPjthPLKcRpnVOb00dnF2e5c4PziIuJS4LLLpc+Lpsbxt3IveRKdPVxXeF60vWdm7Obwu2o26/uNu5p7ofcn8w0nymeWTNz0MPIQ+BR5dE/C5+VMGvfrH5PQ0+BZ7XnIy9jL5FXrdewt6V3qvdh7xc+9j5yn+M+4zw33jLeWV/MN8C3yLfLT8Nvnl+F30N/I/9k/3r/0QCngCUBZwOJgUGBWwL7+Hp8Ib+OPzrbZfay2e1BjKC5QRVBj4KtguXBrSFoyOyQrSH355jOkc5pDoVQfujW0Adh5mGLw34MJ4WHhVeGP45wiFga0TGXNXfR3ENz30T6RJZE3ptnMU85ry1KNSo+qi5qPNo3ujS6P8YuZlnM1VidWElsSxw5LiquNm5svt/87fOH4p3iC+N7F5gvyF1weaHOwvSFpxapLhIsOpZATIhOOJTwQRAqqBaMJfITdyWOCnnCHcJnIi/RNtGI2ENcKh5O8kgqTXqS7JG8NXkkxTOlLOW5hCepkLxMDUzdmzqeFpp2IG0yPTq9MYOSkZBxQqohTZO2Z+pn5mZ2y6xlhbL+xW6Lty8elQfJa7OQrAVZLQq2QqboVFoo1yoHsmdlV2a/zYnKOZarnivN7cyzytuQN5zvn//tEsIS4ZK2pYZLVy0dWOa9rGo5sjxxedsK4xUFK4ZWBqw8uIq2Km3VT6vtV5eufr0mek1rgV7ByoLBtQFr6wtVCuWFfevc1+1dT1gvWd+1YfqGnRs+FYmKrhTbF5cVf9go3HjlG4dvyr+Z3JS0qavEuWTPZtJm6ebeLZ5bDpaql+aXDm4N2dq0Dd9WtO319kXbL5fNKNu7g7ZDuaO/PLi8ZafJzs07P1SkVPRU+lQ27tLdtWHX+G7R7ht7vPY07NXbW7z3/T7JvttVAVVN1WbVZftJ+7P3P66Jqun4lvttXa1ObXHtxwPSA/0HIw6217nU1R3SPVRSj9Yr60cOxx++/p3vdy0NNg1VjZzG4iNwRHnk6fcJ3/ceDTradox7rOEH0x92HWcdL2pCmvKaRptTmvtbYlu6T8w+0dbq3nr8R9sfD5w0PFl5SvNUyWna6YLTk2fyz4ydlZ19fi753GDborZ752PO32oPb++6EHTh0kX/i+c7vDvOXPK4dPKy2+UTV7hXmq86X23qdOo8/pPTT8e7nLuarrlca7nuer21e2b36RueN87d9L158Rb/1tWeOT3dvfN6b/fF9/XfFt1+cif9zsu72Xcn7q28T7xf9EDtQdlD3YfVP1v+3Njv3H9qwHeg89HcR/cGhYPP/pH1jw9DBY+Zj8uGDYbrnjg+OTniP3L96fynQ89kzyaeF/6i/suuFxYvfvjV69fO0ZjRoZfyl5O/bXyl/erA6xmv28bCxh6+yXgzMV70VvvtwXfcdx3vo98PT+R8IH8o/2j5sfVT0Kf7kxmTk/8EA5jz/GMzLdsAAAAgY0hSTQAAeiUAAICDAAD5/wAAgOkAAHUwAADqYAAAOpgAABdvkl/FRgAABlBJREFUeNrcm1mMFFUUhj+aRUQQZZdhkcUFBMRhCYMIIluiCIprNDxINEL0SXhRkAEUMVF5IJLglhADREDFhQdFBVcM6ASFUYZtWHTABTCKMOy/L6djpVLVc6uremH+pDLTdc+9t8+523/POd1AEhco7gcWAilgB/ANsAH4Cjjp2kiDC9gArwKPBLyvAN4AlgK19dkA3YCpQHugBzAQaOop3wTMBD6rrwbwopkZ5A5gCtDd3tcCLwLzgLOBNSXVt6e1pGck1ep/LJfUPEi+Phog/YyUtM1jhPcltfDL1ZclEIZOwHJguH1eAUwGzhfrHtDTNjUBNcD+DLJNgUHAaWAPcDhErg3wjscIC4Cnim0P6CNphaRDnim7T9IiW9NBdZ42ufOStkpaKKlbiGwXSdtN/oyk24ppDxgt6aDCsVFS54B6cwJkD0maEtJPmaTjJlclqVUxGGC0pCOqG5sldfXVvVjSKEkzJG3xyc8N6W+WR2ZmoQ0wUtKfcsf3kjqFtNVE0uOS/vLIPxEg11zST57Z0q5Qyo+S9IeiY7Ot57B2x3qMWmvT3i8z1fYNSZpWCOVHZKl8GhUhe0L6uU/SWZNdJ6lBAFHab+Xr8638LTGVT2NTHTNhlcmdlTQooHypldek8njGDwfeAtom0NZgYA3QJaT8FeAM0BAYF1C+wf52yJcBbgZWJ6R8GqXWZpARNgLH7P9eAeW77W8qlaeRXwW0y0Hb6ZnQ1fe+FnjT2OHagHo/mwOlKtdU+EYbpStybOQK4G5gn+/9pcA/IXWaAI1yaYDhwEqgQx1yf5sb60vgV7uodASGAiOB1hGMMAk4EOlb5mi3H+bj9UE4JellST0ytFMiaYGkY46nw3cZyBL58gcMq4Pby+jvPRGJ0/4IRriyUAYoc1D+uKRxWbQ9IAJ1rgi4O+TcAMMk1Th8uSdj9DElIm0uyZcBhjgqv11Syxj9NJX0bQQjfCipYaY2k+ABQ83j0tFB9gPb9QM91ObhKQMuCpE5aceqK8YDYzIJxDVAGfC2o/ICPs1wJr8ObDYWtxpoGSL7hUvAw4OcGaAsIsk5B+wNKXvA/Plp3A5MC5HdB5yI8D3b5MIAaeVLolCODF/8qoB314bInvB6dR1wMGkDDDZuXxKxXsMMrO4TmyFevBci2wpoHKHfj5NkgkMkHYhxj38wQ9v3mm+vUtLDGeTGSTrt2N9aSamkjsFBMZVPh6jiHrkvOfa1sw6aHckAgyNQ0Uw4LOm6GMp3lvSLQz9VknonxQRvSGDkvXg3wE/n+ixxHPlrkroLDLIIjRyt/rUjX3++rrUZ8MxwaHdH1BlW1+XDdeQXS7rM6vX1RWXD8FqGsJf3uUTSC44DcHXUWRVWMFBStaPylZKa+eqPd6y7S9JjNmpNPPUbmTIPSfrRoZ3dFl+MvKyCPEIDjdt3cTxn1wOjfO+6W8TWFUeAKuCoEabLjRx1cKi7wzJDqrKidD6LlEYY+TRqJHXPYr0mgaxHPmgJDMhCeW/cbpwFK6ZHcGHFQZWkXnF5RXoJ9DPq2e0CyfzYA0ww93YspIzTL3NUfhlwE3CreXELgZ3AxCSUT+8B8xyn3Grf9Glr564LfrOjbIGkPTGm/a4kpr1/D6h07HxCQAOLHOodNDaZrtPTNq9sNry+SXuxUwFhpTAEeX3aO9RbCmzxxeWWRJyou8y9tS3p9ZTK4KPzY7oFJNOYDNzpUO+U47swVAN3ZX3OO+wBKyNMw6N2mfnIk4Tg4gnu5Ns7fohwpe2lHOYsNJA0AlhnjslcoQpYbPm6jwL9HUd+IlCZyyMlzQNmA3MTaO+MhaRbAM1jtLPblldlzg9Vz3R4LiYzq5Y0xu76/SR9HqOdvspT2o73QyqGEc55sy89x93vWZzzeVM+7Do8PwsDnJR0aUBbWyOe872V56y1ILf4LGB+xJXU2EJkfl9/+whH3aTE6G1CCRLPZjF9x9oeUGrusYLQ2yQTJOZENMJpo77/OsrvldS/kPnKLjlCc+2YTBrV5snZRgHhEhorT4gjeHHAsroKqnyU2OCcBI1QbdHfLUXhWom4ZspjkqV9kq4vph9XZVOpPAbDKyrl4+QIzS5mepuvPMHyCNO+tBiVTyJLrNyB5PQpVuWTSpObGhBPqJW0xi5ERf0L0/8GAL3ynCe9nDjjAAAAAElFTkSuQmCC"/>'
        t+='</button>';

        t+='<button onclick="ping_host(\''+encodeURI(rHost)+'\')">';
        t+='<img src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAEAAAABACAYAAACqaXHeAAAACXBIWXMAAAsTAAALEwEAmpwYAAAKT2lDQ1BQaG90b3Nob3AgSUNDIHByb2ZpbGUAAHjanVNnVFPpFj333vRCS4iAlEtvUhUIIFJCi4AUkSYqIQkQSoghodkVUcERRUUEG8igiAOOjoCMFVEsDIoK2AfkIaKOg6OIisr74Xuja9a89+bN/rXXPues852zzwfACAyWSDNRNYAMqUIeEeCDx8TG4eQuQIEKJHAAEAizZCFz/SMBAPh+PDwrIsAHvgABeNMLCADATZvAMByH/w/qQplcAYCEAcB0kThLCIAUAEB6jkKmAEBGAYCdmCZTAKAEAGDLY2LjAFAtAGAnf+bTAICd+Jl7AQBblCEVAaCRACATZYhEAGg7AKzPVopFAFgwABRmS8Q5ANgtADBJV2ZIALC3AMDOEAuyAAgMADBRiIUpAAR7AGDIIyN4AISZABRG8lc88SuuEOcqAAB4mbI8uSQ5RYFbCC1xB1dXLh4ozkkXKxQ2YQJhmkAuwnmZGTKBNA/g88wAAKCRFRHgg/P9eM4Ors7ONo62Dl8t6r8G/yJiYuP+5c+rcEAAAOF0ftH+LC+zGoA7BoBt/qIl7gRoXgugdfeLZrIPQLUAoOnaV/Nw+H48PEWhkLnZ2eXk5NhKxEJbYcpXff5nwl/AV/1s+X48/Pf14L7iJIEyXYFHBPjgwsz0TKUcz5IJhGLc5o9H/LcL//wd0yLESWK5WCoU41EScY5EmozzMqUiiUKSKcUl0v9k4t8s+wM+3zUAsGo+AXuRLahdYwP2SycQWHTA4vcAAPK7b8HUKAgDgGiD4c93/+8//UegJQCAZkmScQAAXkQkLlTKsz/HCAAARKCBKrBBG/TBGCzABhzBBdzBC/xgNoRCJMTCQhBCCmSAHHJgKayCQiiGzbAdKmAv1EAdNMBRaIaTcA4uwlW4Dj1wD/phCJ7BKLyBCQRByAgTYSHaiAFiilgjjggXmYX4IcFIBBKLJCDJiBRRIkuRNUgxUopUIFVIHfI9cgI5h1xGupE7yAAygvyGvEcxlIGyUT3UDLVDuag3GoRGogvQZHQxmo8WoJvQcrQaPYw2oefQq2gP2o8+Q8cwwOgYBzPEbDAuxsNCsTgsCZNjy7EirAyrxhqwVqwDu4n1Y8+xdwQSgUXACTYEd0IgYR5BSFhMWE7YSKggHCQ0EdoJNwkDhFHCJyKTqEu0JroR+cQYYjIxh1hILCPWEo8TLxB7iEPENyQSiUMyJ7mQAkmxpFTSEtJG0m5SI+ksqZs0SBojk8naZGuyBzmULCAryIXkneTD5DPkG+Qh8lsKnWJAcaT4U+IoUspqShnlEOU05QZlmDJBVaOaUt2ooVQRNY9aQq2htlKvUYeoEzR1mjnNgxZJS6WtopXTGmgXaPdpr+h0uhHdlR5Ol9BX0svpR+iX6AP0dwwNhhWDx4hnKBmbGAcYZxl3GK+YTKYZ04sZx1QwNzHrmOeZD5lvVVgqtip8FZHKCpVKlSaVGyovVKmqpqreqgtV81XLVI+pXlN9rkZVM1PjqQnUlqtVqp1Q61MbU2epO6iHqmeob1Q/pH5Z/YkGWcNMw09DpFGgsV/jvMYgC2MZs3gsIWsNq4Z1gTXEJrHN2Xx2KruY/R27iz2qqaE5QzNKM1ezUvOUZj8H45hx+Jx0TgnnKKeX836K3hTvKeIpG6Y0TLkxZVxrqpaXllirSKtRq0frvTau7aedpr1Fu1n7gQ5Bx0onXCdHZ4/OBZ3nU9lT3acKpxZNPTr1ri6qa6UbobtEd79up+6Ynr5egJ5Mb6feeb3n+hx9L/1U/W36p/VHDFgGswwkBtsMzhg8xTVxbzwdL8fb8VFDXcNAQ6VhlWGX4YSRudE8o9VGjUYPjGnGXOMk423GbcajJgYmISZLTepN7ppSTbmmKaY7TDtMx83MzaLN1pk1mz0x1zLnm+eb15vft2BaeFostqi2uGVJsuRaplnutrxuhVo5WaVYVVpds0atna0l1rutu6cRp7lOk06rntZnw7Dxtsm2qbcZsOXYBtuutm22fWFnYhdnt8Wuw+6TvZN9un2N/T0HDYfZDqsdWh1+c7RyFDpWOt6azpzuP33F9JbpL2dYzxDP2DPjthPLKcRpnVOb00dnF2e5c4PziIuJS4LLLpc+Lpsbxt3IveRKdPVxXeF60vWdm7Obwu2o26/uNu5p7ofcn8w0nymeWTNz0MPIQ+BR5dE/C5+VMGvfrH5PQ0+BZ7XnIy9jL5FXrdewt6V3qvdh7xc+9j5yn+M+4zw33jLeWV/MN8C3yLfLT8Nvnl+F30N/I/9k/3r/0QCngCUBZwOJgUGBWwL7+Hp8Ib+OPzrbZfay2e1BjKC5QRVBj4KtguXBrSFoyOyQrSH355jOkc5pDoVQfujW0Adh5mGLw34MJ4WHhVeGP45wiFga0TGXNXfR3ENz30T6RJZE3ptnMU85ry1KNSo+qi5qPNo3ujS6P8YuZlnM1VidWElsSxw5LiquNm5svt/87fOH4p3iC+N7F5gvyF1weaHOwvSFpxapLhIsOpZATIhOOJTwQRAqqBaMJfITdyWOCnnCHcJnIi/RNtGI2ENcKh5O8kgqTXqS7JG8NXkkxTOlLOW5hCepkLxMDUzdmzqeFpp2IG0yPTq9MYOSkZBxQqohTZO2Z+pn5mZ2y6xlhbL+xW6Lty8elQfJa7OQrAVZLQq2QqboVFoo1yoHsmdlV2a/zYnKOZarnivN7cyzytuQN5zvn//tEsIS4ZK2pYZLVy0dWOa9rGo5sjxxedsK4xUFK4ZWBqw8uIq2Km3VT6vtV5eufr0mek1rgV7ByoLBtQFr6wtVCuWFfevc1+1dT1gvWd+1YfqGnRs+FYmKrhTbF5cVf9go3HjlG4dvyr+Z3JS0qavEuWTPZtJm6ebeLZ5bDpaql+aXDm4N2dq0Dd9WtO319kXbL5fNKNu7g7ZDuaO/PLi8ZafJzs07P1SkVPRU+lQ27tLdtWHX+G7R7ht7vPY07NXbW7z3/T7JvttVAVVN1WbVZftJ+7P3P66Jqun4lvttXa1ObXHtxwPSA/0HIw6217nU1R3SPVRSj9Yr60cOxx++/p3vdy0NNg1VjZzG4iNwRHnk6fcJ3/ceDTradox7rOEH0x92HWcdL2pCmvKaRptTmvtbYlu6T8w+0dbq3nr8R9sfD5w0PFl5SvNUyWna6YLTk2fyz4ydlZ19fi753GDborZ752PO32oPb++6EHTh0kX/i+c7vDvOXPK4dPKy2+UTV7hXmq86X23qdOo8/pPTT8e7nLuarrlca7nuer21e2b36RueN87d9L158Rb/1tWeOT3dvfN6b/fF9/XfFt1+cif9zsu72Xcn7q28T7xf9EDtQdlD3YfVP1v+3Njv3H9qwHeg89HcR/cGhYPP/pH1jw9DBY+Zj8uGDYbrnjg+OTniP3L96fynQ89kzyaeF/6i/suuFxYvfvjV69fO0ZjRoZfyl5O/bXyl/erA6xmv28bCxh6+yXgzMV70VvvtwXfcdx3vo98PT+R8IH8o/2j5sfVT0Kf7kxmTk/8EA5jz/GMzLdsAAAAgY0hSTQAAeiUAAICDAAD5/wAAgOkAAHUwAADqYAAAOpgAABdvkl/FRgAABXtJREFUeNrcm22IVUUYx3/3tuSumbplK8VmRiqWZZqkq1YigRBh6JqthUWREFSEUQTRN7OCEiIq/BBE9kppZmUmEeSaBb1gq5kvlBGa7rpumrq7ravuvw/nuXg43Nc557p39g/zYfeeM2fmd+Y8M/M8z6QkEdFcoBGYDtQBncBO4EtgDXCAgSRJmVIvaa3ya6+kuaF7vC8pGwGX2RueWASzHuBeGw1JqxoYB1wJ1ALDgPOBXqALaAP2A78Bp5N4YEpSFfAxcEcJ9x0FZgK7EmjDGOBmYJ51vg64KMe1fcC/QCvQAnwE/AAcivMJzJXUp9K1KsbQq5K0QNJqSd2Kp72Slkka7dIWrCMu+lvSqBIfWCNpsaStjtDz6ZCkZyQNKxXAbscH9kmaU8LD5kjarPLrZ0mzim1XGrjE1X4Aw4u4bgTwMvCFfevl1hRgA/BEMRenzbq6qqfA71OBr4ClQNU5nN0HAysMfKoQAFdLfhzYl+f3u+1NTO7HZc5S4E1gUD4A6x0rbwF25PjtMWAVcHEFrPXuB17JORIk1Un6w8HYzMthWJ5WZeq5XLMAkm6TdLKEyl6TlMpS4ZOqbDXlAoCkhZIOF1HJ6zafRzu/SNKpCgfQKumqXACQdI2ktyT9FbnxqKRmW71lG/YTJR2RH/owPHpTWbbDAKOA8TbP95i1b8mzgdkAzPZkA3zK9j0bM5uhuBU+YFONT9oM3AqcjgtgCPCL7eh802xgUzpmJXd52nmAJZmFkKuqbLXnq6YDI+MAGA80eAzgCuCGdEyCQzwGcF5cALcMAJ/whHQMetMGAIBxrgAuN+el76p1BTABGDoAAAx1BdAKdAwAAGlXAFuBJuCw5wCOxZkFvjEI7R4DOO4KoDYEYaHHEPa5AliaWUvbzqoJ+MdDALtcAaSBV4H59vcmYAFxYnT9o+2uAH4ncIS8bx0HaPbsczgCbHMFsJfAU1RN4P7OjIRvDYIPI2E3sMcVwHaCMDXABcC7BFklGZtwpwcQPgdOuQI4QeAJymhwBMIWM4yVCqEbS/CIsw74LPJ3DfB26HNoNvvQVoEA1thnTJz8mmslHcvidu6W1Bi6bpak9gpyi/dImhkOj7uqlezB0ZqIYWwmSH9prZC3/wnwXXg+d15H51n8DAHeC0H4vkKWzZ3As9EFjatOE2Rv5VLGJjSGpsj5wMF+BPAiQc7jWcVMdNpUxDfXGYkkz5DU1g/f/hZJ1fmCo6WWOsvQUpEQ5ofuvUnSwXPY+XZJk/OFx13KshIbcSICocEyzcqtPot8kxSAtCUbuKS5/ReJMDdIOlDmzj9VKE2ulDJI0sqYjYqOhGmS9pcJwPPF5AkWW0ZIWpdQw7ojI2GGJS+UPSXGFUCDpJaEGxg1jFMTGgknJT1eSqZoobJIUkeZhmhXZCRMi2kT9li+E0kAqJK0XFJvma10tpGwz8GuvCTpUpdk6WylXtL6czhPd0Ug3FjkFNkh6Y1cc3wpBybCmmRL2OsSWHpuJEjEbOVsbvFYYBZwNcGBiIx6gXsIzi5AEHpfDdRH6mw3b84agvzjP+M0MApgknlK6hPo/ErgESBXDs4UghD79QS5BmPMsfIgZ0+jNAAvmANmJ/Ar8KP5JBNRGECN+fmTiPq+Q5A8dabI6wfbDvJCgiyu/SFw1fa/M+XYHYUB3G5vPxWzzq9t/9+FBwpvh0cn0PntwH2+dD4KoDMBP/uSCvL8lAzgJ/PyuKgPeNTqwFcAu3A/O7Ac+MDH6Gh0GhxrrquRJdSxliBfsNdHAOksMb+HgJNF3r8DeNjXzudyin5qc3ghe9AGLMa/iHBBANj33BgJf4XVQXB+eBueq1C2+HBbny8gyAzrIzgGt4LcB6a80v8DANyDCF7l5UiiAAAAAElFTkSuQmCC"/>'
        t+='</button>';


        t+='<button id="rem">'
        t+='<img src="data:image/png;base64,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"/>'
        t+='</button>'
    }





    item.innerHTML = t;

}
var g_colors = ['red', 'blue', 'white', 'yellow'];
var g_color_index = 0;

function ping_host(rHost, s, _n) {

    $do_request(rHost + '/device/stat', function (d) {
        console.log(d, d.N);
        if(d && d.N){
            if(!g_friends[rHost]){
                g_color_index++;
                if (g_color_index > g_colors.length - 1){
                    g_color_index = 0;
                }
                var g_color = g_colors[g_color_index];

                g_friends[rHost] = {
                    name: d.N,
                    color: g_color
                }

            } else {
                g_friends[rHost].name = d.N;
            }

            var nodes = document.getElementsByClassName('hn' + extract_id(rHost));
            for(var i = 0; i < nodes.length; i++){
                nodes[i].innerHTML = d.N;
            }
        }

        if (s){
            s(rHost)
        } else {
            alert('Host '+rHost+' is still running');
        }
    }, function (e) {
        if(_n){
            _n(e);
        } else  {
            console.log('Host '+rHost+' not running anymore')
        }
        $('.' + extract_id(rHost)).hide();
    });
}










var sensors = {};

function readSensors(obj){
    for(var prop in obj){
        if(prop.indexOf("s.") == 0){
            var keys = prop.split(".");
            if (keys.length == 3){

                var key = keys[1];
                if (!sensors[key]){
                    sensors[key] = {};
                }



                sensors[key].name = obj['s.'+ key + ".N"];
                sensors[key].power = obj['s.'+ key + ".P"];
                sensors[key].accuracy = obj['s.'+ key + ".A"];
                sensors[key].version = obj['s.'+ key + ".V"];
                sensors[key].vendor = obj['s.'+ key + ".vd"];
                sensors[key].values = obj['s.'+ key + ".L"];
                if (sensors[key].values){
                    sensors[key].values = sensors[key].values.split(",");
                }



            }
        }
    }


    for(var key in sensors){
        if (sensors[key].values && 0 < sensors[key].values.length ){
            placeSensorInDOM(sensors[key], key)
        }
    }
}


function placeSensorInDOM(sensor, id){
    var div;
    id =  "s_dinfo_" + id;

    if (null == document.getElementById(id) || !document.getElementById(id)){
        div = document.createElement('tr');
        div.setAttribute('class', 'd-info sensor-data');
        div.id = id;
        $('#sensors-data').append(div);
        var innerHTML = ' <td class="dev-lbl sensor-name"> ' + sensor.name + ' ' + sensor.version +  ' </td>';
        innerHTML += ' <td class="dev-lbl sensor-info"> ' + sensor.vendor + ' P'  + sensor.power + ' A' + sensor.accuracy +   ' </td>';
        if (sensor.values){
            innerHTML +='<td id="s_' + id + '_values" class="sensor-values"> ' + sensor.values + '</td>';
        }
        div.innerHTML = innerHTML;
    } else {
        div = document.getElementById(id);
        console.log("place " + id + "REUSE");
    }
}



function updateProps(props){
    if ("vlc" === props.cbin){
        $('#nwi-btn').show();
        document.getElementById('nwif').src = 'http://' + props['vlc-host'];
    }
    else {
        $('#nwi-btn').hide();
    }


    if (true == props.ks || 'true' == props.ks){
        _h();
        $('#controls').show();
    }
}


function close_app() {
    if(confirm('are you sure you want to close application?')){
        $get('/close-app');
    }
}


function prompt_for_host(s, c) {
    var txt =  prompt("Enter host name");
    if(txt === null || txt == null || txt === ''){
        if(c){
            c(txt);
        }
        return;
    }
    get_host_from_text(txt, s, c);
}

function prompt_for_valid_host(s, e) {
    prompt_for_host(function (hh) {
        var th = hh + '/device/stat';
        $do_request(th, function (d) {
            s(hh);
        });
    }, function (x) {
        e(x);
    });
}

function add_friend_host(){
    prompt_for_valid_host(function (host) {
        if (confirm('Add ' + host + ' as friend?')){
            try {
                place_ip('ips-container', host, 'panel');
            }catch (e) {
                alert(e);
            }
            AppSettings.addFriendIp(host);
        }
    }, function (x) {
        console.log("invalid host for " + x);
    })



}


function reconnect() {
    var text = prompt("Enter device ip");
    if(text === null || text == null || text === ''){
        return;
    }
    get_host_from_text(text, function (rHost) {
        loadUrl(rHost + '/app');
    }, function (t) {
        console.log("invalid input " + t)
    })
}


function loadUrl(xurl) {

    if( (typeof quixot != 'undefined') && ( typeof quixot.loadUrl != 'undefined')){
        quixot.loadUrl(xurl);
    } else {
        window.location = xurl;
    }
}

function remoteControl(rHost) {
    ping_host(rHost, function (s) {
        loadUrl(rHost + '/app');
    }, function () {
        alert("Cannot remote control " + rHost);
    })
}


function get_host_from_text(x, c, e) {
    if(x ==  null || x == ''){
        if(e){
            e(x);
        } else {
            console.log('Invalid input ' + x);
        }
        return;
    }

    if(x.indexOf("http://") != 0 && x.indexOf("https://") != 0){
        x = 'http://' + x;
    }
    ping_host(x,
        function (rhost) {
             c(rhost);
        },
        function (ex) {
             if(e){
                 e(x);
             } else {
                 console.log('Invalid input ' + x);
             }
        }
    )
}

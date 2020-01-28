[
    {
        "id": "a2006c93.7ceb8",
        "type": "tab",
        "label": "Flow 1",
        "disabled": false,
        "info": ""
    },
    {
        "id": "ed673641.326268",
        "type": "function",
        "z": "a2006c93.7ceb8",
        "name": "legend",
        "func": "msg = {};\nvar legend = \"Legend\";\nvar large = \"●: Mw ≥ 5.8\";\nvar medium = \"■: 4.6 ≤ Mw < 5.8\";\nvar average = \"■: 3.6 ≤ Mw < 4.6\";\nvar small = \"▬: Mw < 3.6\";\nvar current = \"▲: Current event\";\nvar easy = \"○: Node event\";\nmsg.lgnd=legend;\nmsg.large=large;\nmsg.medium=medium;\nmsg.average=average;\nmsg.small=small;\nmsg.curr=current; \nmsg.easy=easy;\nreturn msg;",
        "outputs": 1,
        "noerr": 0,
        "x": 475,
        "y": 466,
        "wires": [
            [
                "40a0d7f7.51d8a8"
            ]
        ]
    },
    {
        "id": "6ce6bd2e.1e76f4",
        "type": "function",
        "z": "a2006c93.7ceb8",
        "name": "getAzi",
        "func": "return {payload: msg.payload.direction};",
        "outputs": 1,
        "noerr": 0,
        "x": 475,
        "y": 80,
        "wires": [
            [
                "b8077096.fd96f"
            ]
        ]
    },
    {
        "id": "806c50b2.8d734",
        "type": "function",
        "z": "a2006c93.7ceb8",
        "name": "getThreat",
        "func": "return {payload: msg.payload.threat};",
        "outputs": 1,
        "noerr": 0,
        "x": 485,
        "y": 115,
        "wires": [
            [
                "11804ad1.eaf575"
            ]
        ]
    },
    {
        "id": "23e321dc.bc8d5e",
        "type": "function",
        "z": "a2006c93.7ceb8",
        "name": "getMwlog",
        "func": "return {payload: msg.payload.mag};",
        "outputs": 1,
        "noerr": 0,
        "x": 485,
        "y": 150,
        "wires": [
            [
                "29dc132a.3b805c"
            ]
        ]
    },
    {
        "id": "b3fcb7d4.410e28",
        "type": "function",
        "z": "a2006c93.7ceb8",
        "name": "getCoord",
        "func": "var thing = {\n    name:msg.payload.nodename, \n    lat:msg.payload.lat, \n    lon:msg.payload.lon,\n    icon:\"earthquake\",\n    mag:msg.payload.mag,\n    addtoheatmap: \"false\",\n    layer: \"EASYfeed\",\n    time:msg.payload.time\n};\nmsg = {};\nmsg.payload = thing;\nmsg.topic = \"Dweetio\";\nreturn msg;",
        "outputs": 1,
        "noerr": 0,
        "x": 485,
        "y": 220,
        "wires": [
            [
                "731fc1e6.6864c"
            ]
        ]
    },
    {
        "id": "670e9d83.62a5a4",
        "type": "json",
        "z": "a2006c93.7ceb8",
        "name": "ReadingJSON",
        "property": "payload",
        "action": "obj",
        "pretty": true,
        "x": 285,
        "y": 135,
        "wires": [
            [
                "6ce6bd2e.1e76f4",
                "806c50b2.8d734",
                "23e321dc.bc8d5e",
                "b3fcb7d4.410e28",
                "2e7310e6.dc1f3",
                "4cee4614.b2dd78"
            ]
        ]
    },
    {
        "id": "1b98ee83.8c90d1",
        "type": "template",
        "z": "a2006c93.7ceb8",
        "name": "embedMap",
        "field": "payload",
        "fieldType": "msg",
        "format": "handlebars",
        "syntax": "mustache",
        "template": "<iframe src={{{payload}}} width =580 height=500 ></iframe>",
        "x": 495,
        "y": 361,
        "wires": [
            [
                "23a5d52d.e720da"
            ]
        ]
    },
    {
        "id": "bd05aded.08c28",
        "type": "inject",
        "z": "a2006c93.7ceb8",
        "name": "",
        "topic": "",
        "payload": "/worldmap",
        "payloadType": "str",
        "repeat": "",
        "crontab": "",
        "once": true,
        "onceDelay": "0.01",
        "x": 270,
        "y": 416,
        "wires": [
            [
                "ed673641.326268",
                "1b98ee83.8c90d1",
                "cad5d60b.08f028",
                "59728245.03228c"
            ]
        ]
    },
    {
        "id": "7625fa4d.e5e1a4",
        "type": "twitter out",
        "z": "a2006c93.7ceb8",
        "twitter": "",
        "name": "Tweet @easyalertsystem",
        "x": 740,
        "y": 45,
        "wires": []
    },
    {
        "id": "2e7310e6.dc1f3",
        "type": "function",
        "z": "a2006c93.7ceb8",
        "name": "getTweetData",
        "func": "var lat = msg.payload.lat;\nvar lon = msg.payload.lon;\nvar mag = msg.payload.mag;\nvar sta = msg.payload.nodename;\nvar time =msg.payload.time;\nvar tweetText = \"(TEST MSG)New event recorded at: \"+ time +\" from station: \" + sta + \" at: http://maps.google.com/maps?q=\" + lat + \",\" + lon + \". Estimated magnitude: \" + mag + \". See more: https://goo.gl/ZLJvUk\";\nreturn {payload: tweetText};",
        "outputs": 1,
        "noerr": 0,
        "x": 505,
        "y": 45,
        "wires": [
            [
                "7625fa4d.e5e1a4"
            ]
        ]
    },
    {
        "id": "aa3d1bd6.a506c8",
        "type": "function",
        "z": "a2006c93.7ceb8",
        "name": "logger",
        "func": "var mwlog = context.get('mwlog')|| [];\n\nmwlog.push(msg);\nif (mwlog.length > 20){\n    mwlog.shift();\n    mwlog.length = 20;\n} \n\ncontext.set('mwlog',mwlog);\n\nmsg = {};\nreturn {payload: mwlog};\n",
        "outputs": 1,
        "noerr": 0,
        "x": 675,
        "y": 185,
        "wires": [
            [
                "f572548.67a2ca8"
            ]
        ]
    },
    {
        "id": "4cee4614.b2dd78",
        "type": "function",
        "z": "a2006c93.7ceb8",
        "name": "getMwlog",
        "func": "var lat = msg.payload.lat;\nvar lon = msg.payload.lon;\nvar mag = msg.payload.mag;\nvar threat = msg.payload.threat;\nvar node = msg.payload.nodename;\nvar time = msg.payload.time;\n\n\nreturn {payload: \"Node: \" + node + \" Mw: \" + mag  + '\\n\\r' +  \"Threat: \" + threat +  \"%\"  + '\\n\\r' + \"At: \" + time  + '\\n\\r' + \" Coordinates: \" + lat  +  \" - \" + lon};",
        "outputs": 1,
        "noerr": 0,
        "x": 485,
        "y": 185,
        "wires": [
            [
                "aa3d1bd6.a506c8"
            ]
        ]
    },
    {
        "id": "75f614de.4708ac",
        "type": "function",
        "z": "a2006c93.7ceb8",
        "name": "rssLog",
        "func": "var rssLog = context.get('rssLog')|| [];\nvar len = global.get(\"xmlLen\");\nrssLog.push(msg);\nif (rssLog.length > len){\n    rssLog.shift();\n    rssLog.length = len;\n} \n//rssLog.reverse();\n\ncontext.set('rssLog',rssLog);\nmsg = {};\nmsg.payload = rssLog;\nreturn {payload: rssLog};\n",
        "outputs": 1,
        "noerr": 0,
        "x": 1000,
        "y": 361,
        "wires": [
            [
                "73f8d4d1.e258ec"
            ]
        ]
    },
    {
        "id": "22aaf743.a86ef8",
        "type": "inject",
        "z": "a2006c93.7ceb8",
        "name": "",
        "topic": "",
        "payload": "",
        "payloadType": "date",
        "repeat": "30",
        "crontab": "",
        "once": true,
        "onceDelay": "1",
        "x": 275,
        "y": 326,
        "wires": [
            [
                "a403981d.6f9678"
            ]
        ]
    },
    {
        "id": "d4db1bb5.301c78",
        "type": "xml",
        "z": "a2006c93.7ceb8",
        "name": "",
        "property": "payload",
        "attr": "",
        "chr": "",
        "x": 675,
        "y": 326,
        "wires": [
            [
                "28a2582c.61bb88",
                "9e34512d.ee29a"
            ]
        ]
    },
    {
        "id": "a403981d.6f9678",
        "type": "http request",
        "z": "a2006c93.7ceb8",
        "name": "",
        "method": "GET",
        "ret": "txt",
        "url": "http://www.geophysics.geol.uoa.gr/stations/maps/seismicity.xml",
        "tls": "",
        "x": 495,
        "y": 326,
        "wires": [
            [
                "d4db1bb5.301c78"
            ]
        ]
    },
    {
        "id": "28a2582c.61bb88",
        "type": "function",
        "z": "a2006c93.7ceb8",
        "name": "rssParse",
        "func": "var i;\nvar len = msg.payload.rss.channel[0].item.length;\nglobal.set(\"xmlLen\",len);\nvar outputMsgs = [];\n\nfor (i = 0; i < len; i++) {\n  var data = msg.payload.rss.channel[0].item[i].description[0].replace(/<br\\s*\\/?>/gi, ' ');\n  var a = data.split(\" \");\n  var ico = \"\";\n  if ( i == 0 ) {\n    ico = \"danger\";\n  }\n  else {\n    if (parseFloat(a[25])>=5.8) {\n      ico = \"hostile\";\n    } else if ((parseFloat(a[25])>=4.6)&&(parseFloat(a[25])<5.8)) {\n      ico = \"unknown\";\n    } else if ((parseFloat(a[25])>=3.6)&&(parseFloat(a[25])<4.6)) {\n      ico = \"neutral\";\n    } else {\n      ico = \"friend\";\n    }\n  }\n  var thing = {\n    name: a[0] + \" \" + a[1] + \" \" + a[2] + \" \" + a[3] + \" \" + a[4],\n    lat: parseFloat(a[13].slice(0, -1)),\n    lon: parseFloat(a[17].slice(0, -1)),\n    icon: ico,\n    mag: parseFloat(a[25]),\n    iconColor: \"#ffcccc\",\n    time: (a[7] + \" \" + a[8] + \" \" + a[9]),\n    addtoheatmap: \"false\",\n    layer: \"UOAfeed\",\n    weblink: {name:\"more...\", url:msg.payload.rss.channel[0].item[i].link[0]}\n  };\n  outputMsgs.push({payload:thing});\n}\nreturn [outputMsgs];\n",
        "outputs": 1,
        "noerr": 0,
        "x": 845,
        "y": 326,
        "wires": [
            [
                "731fc1e6.6864c"
            ]
        ]
    },
    {
        "id": "731fc1e6.6864c",
        "type": "worldmap",
        "z": "a2006c93.7ceb8",
        "name": "Events",
        "lat": "37.98",
        "lon": "23.72",
        "zoom": "6",
        "layer": "OSM grey",
        "cluster": "",
        "maxage": "86400",
        "usermenu": "hide",
        "layers": "show",
        "panit": "false",
        "x": 1000,
        "y": 225,
        "wires": []
    },
    {
        "id": "11804ad1.eaf575",
        "type": "ui_gauge",
        "z": "a2006c93.7ceb8",
        "name": "Threat",
        "group": "4f08b772.2020b8",
        "order": 0,
        "width": "3",
        "height": "3",
        "gtype": "gage",
        "title": "Threat",
        "label": "%",
        "format": "{{value}}",
        "min": 0,
        "max": "100",
        "colors": [
            "#00b500",
            "#e6e600",
            "#ca3838"
        ],
        "seg1": "",
        "seg2": "",
        "x": 675,
        "y": 115,
        "wires": []
    },
    {
        "id": "b8077096.fd96f",
        "type": "ui_gauge",
        "z": "a2006c93.7ceb8",
        "name": "",
        "group": "4f08b772.2020b8",
        "order": 0,
        "width": 0,
        "height": 0,
        "gtype": "compass",
        "title": "Azimuth",
        "label": "°",
        "format": "{{value}}",
        "min": 0,
        "max": "360",
        "colors": [
            "#00b500",
            "#e6e600",
            "#ca3838"
        ],
        "seg1": "",
        "seg2": "",
        "x": 688,
        "y": 80,
        "wires": []
    },
    {
        "id": "29dc132a.3b805c",
        "type": "ui_chart",
        "z": "a2006c93.7ceb8",
        "name": "magnitudePlot",
        "group": "9f863f68.91fb7",
        "order": 0,
        "width": "9",
        "height": "5",
        "label": "",
        "chartType": "line",
        "legend": "false",
        "xformat": "HH:mm:ss",
        "interpolate": "linear",
        "nodata": "",
        "dot": false,
        "ymin": "0",
        "ymax": "10",
        "removeOlder": 1,
        "removeOlderPoints": "",
        "removeOlderUnit": "3600",
        "cutout": 0,
        "useOneColor": false,
        "colors": [
            "#1f77b4",
            "#aec7e8",
            "#ff7f0e",
            "#2ca02c",
            "#98df8a",
            "#d62728",
            "#ff9896",
            "#9467bd",
            "#c5b0d5"
        ],
        "useOldStyle": false,
        "x": 705,
        "y": 150,
        "wires": [
            [],
            []
        ]
    },
    {
        "id": "23a5d52d.e720da",
        "type": "ui_template",
        "z": "a2006c93.7ceb8",
        "group": "15dfde8f.cb6381",
        "name": "map",
        "order": 1,
        "width": "12",
        "height": "10",
        "format": "<div ng-bind-html=\"msg.payload | trusted\"></div>",
        "storeOutMessages": true,
        "fwdInMessages": true,
        "templateScope": "local",
        "x": 675,
        "y": 361,
        "wires": [
            []
        ]
    },
    {
        "id": "f572548.67a2ca8",
        "type": "ui_template",
        "z": "a2006c93.7ceb8",
        "group": "3aad0b9d.3d01d4",
        "name": "logTemplate",
        "order": 1,
        "width": "4",
        "height": "4",
        "format": "    <font size=\"2\">\n    <ul>\n    <li ng-repeat=\" x in msg.payload| orderBy:'-'\">\n        {{x.payload}}     \n    </li>\n    </ul>\n    </font>",
        "storeOutMessages": true,
        "fwdInMessages": true,
        "templateScope": "local",
        "x": 855,
        "y": 185,
        "wires": [
            []
        ]
    },
    {
        "id": "73f8d4d1.e258ec",
        "type": "ui_template",
        "z": "a2006c93.7ceb8",
        "group": "51c2b92b.f9fb48",
        "name": "earthquakeList",
        "order": 1,
        "width": "24",
        "height": "10",
        "format": "<ul>\n <li ng-repeat=\"x in msg.payload\">\n     <a target=\"_blank\" href={{x.topic}}>{{x.payload}}</a>\n </li>\n</ul>",
        "storeOutMessages": true,
        "fwdInMessages": true,
        "templateScope": "local",
        "x": 1175,
        "y": 361,
        "wires": [
            []
        ]
    },
    {
        "id": "51a790a2.56a5",
        "type": "dweetio in",
        "z": "a2006c93.7ceb8",
        "thing": "java-client-easy-iot-manager",
        "name": "datafeed",
        "x": 100,
        "y": 135,
        "wires": [
            [
                "670e9d83.62a5a4"
            ]
        ]
    },
    {
        "id": "cad5d60b.08f028",
        "type": "function",
        "z": "a2006c93.7ceb8",
        "name": "About",
        "func": "msg = {};\nvar pro = \"Project:\";\nvar prd = \"Earthquake Alert SYstem (EASY)\";\nvar uni = \"National & Kapodistrian University of Athens. Faculty of Geology and Geoenvironment.\";\nvar inf = \"Author: Stylianos Voutsinas, Athens 2018\";\n\nmsg.pro=pro;\nmsg.prd=prd;\nmsg.uni=uni;\nmsg.inf=inf;\nreturn msg;",
        "outputs": 1,
        "noerr": 0,
        "x": 475,
        "y": 396,
        "wires": [
            [
                "d91ea27e.daa69"
            ]
        ]
    },
    {
        "id": "59728245.03228c",
        "type": "function",
        "z": "a2006c93.7ceb8",
        "name": "Help",
        "func": "msg = {};\nvar sta = \"Stations Data:\";\nvar map = \"Map: Interractive map that displays Seismic events from the UOA seismic network, and from the EASY nodes.\";\nvar log = \"Log: Displays the 20 last events that have benn recorded by an easy node.\";\nvar mag = \"Magnitude plot: Plots magnitude vs time. Data received from EASY nodes.\";\nvar nod = \"Node data: Displays the current Azimuth and threat level of the last event recoded by an EASY node.\";\nvar rss = \"Recent earthquakes: A list of Earthqueakes from the UOA RssFeed.\";\nvar nct = \"Node configuration tool:\";\nvar nch = \"Creates the nodesettings.conf file necessary for the Easy Application\";\nvar hl = \"Modified Mercalli Intensity Scale:\";\nvar hml = \"By selecting the EASYfeed layer, in addition to the other available information given on the app, A colored layer appears on map displaying the potential damages caused by the earthquake. <br> Colors are based on the Modified Mercalli Intensity scale and are shown on the table below\";\n\nmsg.sta=sta;\nmsg.map=map;\nmsg.log=log;\nmsg.mag=mag;\nmsg.nod=nod; \nmsg.rss=rss;\nmsg.nct=nct;\nmsg.nch=nch;\nmsg.hl=hl;\nmsg.hml=hml;\nreturn msg;\n",
        "outputs": 1,
        "noerr": 0,
        "x": 475,
        "y": 431,
        "wires": [
            [
                "69203dbf.ec5894"
            ]
        ]
    },
    {
        "id": "40a0d7f7.51d8a8",
        "type": "ui_template",
        "z": "a2006c93.7ceb8",
        "group": "37dcb89.ee55b48",
        "name": "legendTemplate",
        "order": 1,
        "width": "4",
        "height": "4",
        "format": "<div id = \"legend\" ng-bind-html=\"msg.lgnd\"></div>\n<div id = \"large\" ng-bind-html=\"msg.large\"></div>\n<div id = \"medium\" ng-bind-html=\"msg.medium\"></div>\n<div id = \"average\" ng-bind-html=\"msg.average\"></div>\n<div id = \"small\" ng-bind-html=\"msg.small\"></div>\n<div id = \"current\" ng-bind-html=\"msg.curr\"></div>\n<div id = \"easy\" ng-bind-html=\"msg.easy\"></div>\n\n<script>\n    (function(scope) {\n        document.getElementById('legend').style.color = \"black\";\n        document.getElementById('legend').style.fontWeight = \"bold\";\n        document.getElementById('legend').style.textDecoration = \"underline\"; \n        document.getElementById('large').style.color = \"red\";\n        document.getElementById('medium').style.color = \"goldenrod\";\n        document.getElementById('average').style.color = \"green\";\n        document.getElementById('small').style.color = \"blue\";\n        document.getElementById('current').style.color = \"red\";\n        document.getElementById('easy').style.color = \"black\";\n    })(scope);\n</script>",
        "storeOutMessages": true,
        "fwdInMessages": true,
        "templateScope": "local",
        "x": 705,
        "y": 466,
        "wires": [
            []
        ]
    },
    {
        "id": "69203dbf.ec5894",
        "type": "ui_template",
        "z": "a2006c93.7ceb8",
        "group": "631e82d4.384a7c",
        "name": "helpTemplate",
        "order": 0,
        "width": "20",
        "height": "7",
        "format": "<div id = \"sta\" ng-bind-html=\"msg.sta\"></div>\n<div id = \"map\" ng-bind-html=\"msg.map\"></div>\n<div id = \"log\" ng-bind-html=\"msg.log\"></div>\n<div id = \"mag\" ng-bind-html=\"msg.mag\"></div>\n<div id = \"nod\" ng-bind-html=\"msg.nod\"></div>\n<div id = \"rss\" ng-bind-html=\"msg.rss\"></div>\n<div id = \"nct\" ng-bind-html=\"msg.nct\"></div>\n<div id = \"nch\" ng-bind-html=\"msg.nch\"></div>\n<div id = \"hl\" ng-bind-html=\"msg.hl\"></div>\n<div id = \"hml\" ng-bind-html=\"msg.hml\"></div>\n\n\n<script>\n    (function(scope) {\n        document.getElementById('sta').style.color = \"black\";\n        document.getElementById('sta').style.fontWeight = \"bold\";\n        document.getElementById('sta').style.textDecoration = \"underline\";\n        document.getElementById('map').style.color = \"black\";\n        document.getElementById('log').style.color = \"black\";\n        document.getElementById('mag').style.color = \"black\";\n        document.getElementById('nod').style.color = \"black\";\n        document.getElementById('rss').style.color = \"black\";\n        document.getElementById('nct').style.color = \"black\";\n        document.getElementById('nct').style.fontWeight = \"bold\";\n        document.getElementById('nct').style.textDecoration = \"underline\";        \n        document.getElementById('nch').style.color = \"black\";\n        document.getElementById('hl').style.color = \"black\";\n        document.getElementById('hl').style.fontWeight = \"bold\";\n        document.getElementById('hl').style.textDecoration = \"underline\"; \n        document.getElementById('hml').style.color = \"black\";\n\n\n    })(scope);\n</script>\n<style type=\"text/css\">\n.tg  {border-collapse:collapse;border-spacing:0;}\n.tg td{font-family:Arial, sans-serif;font-size:14px;padding:10px 5px;border-style:solid;border-width:0px;overflow:hidden;word-break:normal;border-color:black;}\n.tg th{font-family:Arial, sans-serif;font-size:14px;font-weight:normal;padding:10px 5px;border-style:solid;border-width:0px;overflow:hidden;word-break:normal;border-color:black;}\n.tg .tg-obcv{border-color:#000000;text-align:center}\n.tg .tg-0bg9{background-color:#32cb00;border-color:#000000;text-align:center;vertical-align:top}\n.tg .tg-wp8o{border-color:#000000;text-align:center;vertical-align:top}\n.tg .tg-4pze{font-weight:bold;text-decoration:underline;border-color:#000000;text-align:center}\n.tg .tg-yes0{font-weight:bold;border-color:#000000;text-align:center}\n.tg .tg-6ghe{background-color:#3166ff;border-color:#000000;text-align:center}\n.tg .tg-a8qm{background-color:#67fd9a;border-color:#000000;text-align:center;vertical-align:top}\n.tg .tg-txdf{background-color:#f8ff00;border-color:#000000;text-align:center;vertical-align:top}\n.tg .tg-3ibu{background-color:#f56b00;border-color:#000000;text-align:center;vertical-align:top}\n.tg .tg-gy3q{background-color:#cb0000;border-color:#000000;text-align:center;vertical-align:top}\n.tg .tg-mqa1{font-weight:bold;border-color:#000000;text-align:center;vertical-align:top}\n</style>\n<table class=\"tg\">\n  <tr>\n    <th class=\"tg-4pze\" colspan=\"7\">MMI scale</th>\n  </tr>\n  <tr>\n    <td class=\"tg-yes0\">PGA (m/s²)</td>\n    <td class=\"tg-obcv\">0.016677</td>\n    <td class=\"tg-wp8o\">0.38259</td>\n    <td class=\"tg-wp8o\">0.90252</td>\n    <td class=\"tg-wp8o\">1.7658</td>\n    <td class=\"tg-wp8o\">3.3354</td>\n    <td class=\"tg-wp8o\">6.3765</td>\n  </tr>\n  <tr>\n    <td class=\"tg-yes0\">MMI</td>\n    <td class=\"tg-6ghe\">I</td>\n    <td class=\"tg-0bg9\">II-IV</td>\n    <td class=\"tg-a8qm\">V</td>\n    <td class=\"tg-txdf\">VI</td>\n    <td class=\"tg-3ibu\">VII</td>\n    <td class=\"tg-gy3q\">VIII+</td>\n  </tr>\n  <tr>\n    <td class=\"tg-mqa1\">Potential<br>Damage</td>\n    <td class=\"tg-wp8o\">None</td>\n    <td class=\"tg-wp8o\">None</td>\n    <td class=\"tg-wp8o\">Very light</td>\n    <td class=\"tg-wp8o\">Light</td>\n    <td class=\"tg-wp8o\">Moderate</td>\n    <td class=\"tg-wp8o\">Moderate<br> to heavy</td>\n  </tr>\n</table>",
        "storeOutMessages": true,
        "fwdInMessages": true,
        "templateScope": "local",
        "x": 695,
        "y": 431,
        "wires": [
            []
        ]
    },
    {
        "id": "d91ea27e.daa69",
        "type": "ui_template",
        "z": "a2006c93.7ceb8",
        "group": "997921e.ec465e",
        "name": "aboutTemplate",
        "order": 0,
        "width": 0,
        "height": 0,
        "format": "<div id = \"pro\" ng-bind-html=\"msg.pro\"></div>\n<div id = \"prd\" ng-bind-html=\"msg.prd\"></div>\n<div id = \"uni\" ng-bind-html=\"msg.uni\"></div>\n<div id = \"inf\" ng-bind-html=\"msg.inf\"></div>\n\n<script>\n    (function(scope) {\n        document.getElementById('pro').style.color = \"black\";\n        document.getElementById('pro').style.fontWeight = \"bold\";\n        document.getElementById('pro').style.textDecoration = \"underline\";\n        document.getElementById('prd').style.color = \"black\";\n        document.getElementById('uni').style.color = \"black\";\n        document.getElementById('inf').style.color = \"black\";\n    })(scope);\n</script>",
        "storeOutMessages": true,
        "fwdInMessages": true,
        "templateScope": "local",
        "x": 705,
        "y": 396,
        "wires": [
            []
        ]
    },
    {
        "id": "c078b162.1d816",
        "type": "dweetio in",
        "z": "a2006c93.7ceb8",
        "thing": "java-client-easy-iot-manager-pga",
        "name": "datafeedPGA",
        "x": 105,
        "y": 255,
        "wires": [
            [
                "1e256cf5.5e0803"
            ]
        ]
    },
    {
        "id": "1e256cf5.5e0803",
        "type": "json",
        "z": "a2006c93.7ceb8",
        "name": "ReadingJSON",
        "property": "payload",
        "action": "obj",
        "pretty": true,
        "x": 285,
        "y": 255,
        "wires": [
            [
                "85520562.9df2d8"
            ]
        ]
    },
    {
        "id": "85520562.9df2d8",
        "type": "function",
        "z": "a2006c93.7ceb8",
        "name": "getPGA",
        "func": "var color; \nvar acc = msg.payload.pga;\nif (acc < 0.016677) {\n    color = 'blue';\n} else if ((acc >= 0.016677) && (acc < 0.38259)) {\n    color = 'yellowgreen';\n} else if ((acc >= 0.38259) && (acc < 0.90252)) {\n    color = 'aqua';\n} else if ((acc >= 0.90252) && (acc < 1.7658)) {\n    color = 'yellow';\n} else if ((acc >= 1.7658) && (acc < 3.3354)) {\n    color = 'orange';\n} else {\n    color = 'red';\n}\nvar thing = {\n    pga: msg.payload.pga,\n    name: msg.payload.nodename,\n    lat: msg.payload.lat,\n    lon: msg.payload.lon,\n    icon: \"earthquake\",\n    time: msg.payload.time,\n    layer: \"EASYfeed\",\n    ttl:3600,\n    radius:1000,\n    addtoheatmap: false,\n    fillColor: color,\n    stroke:false,\n    fillOpacity:0.6,\n    clickable:true,\n};\nmsg = {};\nmsg.payload = thing;\nreturn msg;",
        "outputs": 1,
        "noerr": 0,
        "x": 485,
        "y": 255,
        "wires": [
            [
                "6fca14c9.ae2a8c",
                "731fc1e6.6864c"
            ]
        ]
    },
    {
        "id": "646adf20.964b6",
        "type": "function",
        "z": "a2006c93.7ceb8",
        "name": "pgalogger",
        "func": "var easyLog = context.get('easyLog')|| [];\nvar len = global.get(\"xmlLen\");\neasyLog.push(msg);\nif (easyLog.length > len){\n    easyLog.shift();\n    easyLog.length = len;\n} \n\ncontext.set('easyLog',easyLog);\nmsg = {};\nmsg.payload = easyLog;\nreturn {payload: easyLog};\n",
        "outputs": 1,
        "noerr": 0,
        "x": 845,
        "y": 290,
        "wires": [
            [
                "610799d5.fe7eb8"
            ]
        ]
    },
    {
        "id": "610799d5.fe7eb8",
        "type": "ui_template",
        "z": "a2006c93.7ceb8",
        "group": "82a00fb3.45dbb",
        "name": "pgaTemplate",
        "order": 0,
        "width": "4",
        "height": "4",
        "format": "    <font size=\"2\">\n    <ul>\n    <li ng-repeat=\" x in msg.payload | orderBy:'-'\">\n        {{x.payload}}     \n    </li>\n    </ul>\n    </font>",
        "storeOutMessages": true,
        "fwdInMessages": true,
        "templateScope": "local",
        "x": 1020,
        "y": 290,
        "wires": [
            []
        ]
    },
    {
        "id": "6fca14c9.ae2a8c",
        "type": "function",
        "z": "a2006c93.7ceb8",
        "name": "getPGAlog",
        "func": "var lat = msg.payload.lat;\nvar lon = msg.payload.lon;\nvar pga = parseFloat(msg.payload.pga);\nvar node = msg.payload.name;\nvar time = msg.payload.time;\n\nfunction mmi(acc) {\nvar dmg; \nif (acc < 0.016677) {\n    dmg = \"None\";\n} else if ((acc >= 0.016677) && (acc < 0.38259)) {\n    dmg = \"None\";\n} else if ((acc >= 0.38259) && (acc < 0.90252)) {\n    dmg = \"Very light\";\n} else if ((acc >= 0.90252) && (acc < 1.7658)) {\n    dmg = \"Light\";\n} else if ((acc >= 1.7658) && (acc < 3.3354)) {\n    dmg = \"Moderate\";\n} else {\n    dmg = \"Moderate to Heavy\";\n}\n  return dmg;\n}\n\nreturn {payload: \"Node: \" + node + \" Potential Damage: \" + mmi(pga.toFixed(5)) + '\\n\\r' +  \" At: \" + time  + '\\n\\r' +  \"Coordinates: \" + lat  +  \" - \" + lon};",
        "outputs": 1,
        "noerr": 0,
        "x": 695,
        "y": 290,
        "wires": [
            [
                "646adf20.964b6"
            ]
        ]
    },
    {
        "id": "9e34512d.ee29a",
        "type": "function",
        "z": "a2006c93.7ceb8",
        "name": "rsslist",
        "func": "var i;\nvar len = msg.payload.rss.channel[0].item.length;\nvar outputMsgs = [];\n\nfor (i = 0; i < len; i++) {\n  var data = msg.payload.rss.channel[0].item[i].description[0].replace(/<br\\s*\\/?>/gi, ' ');\n  var topic = msg.payload.rss.channel[0].item[i].link[0]\n  var msg1 = {}\n  msg1.payload = data;\n  msg1.topic = topic;\n  outputMsgs.push(msg1);\n}\nreturn [outputMsgs];\n",
        "outputs": 1,
        "noerr": 0,
        "x": 835,
        "y": 361,
        "wires": [
            [
                "75f614de.4708ac"
            ]
        ]
    },
    {
        "id": "4f08b772.2020b8",
        "type": "ui_group",
        "z": "a2006c93.7ceb8",
        "name": "Node data",
        "tab": "f783fcbb.2d6e2",
        "order": 6,
        "disp": true,
        "width": "3",
        "collapse": true
    },
    {
        "id": "9f863f68.91fb7",
        "type": "ui_group",
        "z": "a2006c93.7ceb8",
        "name": "Magnitude plot",
        "tab": "f783fcbb.2d6e2",
        "order": 5,
        "disp": true,
        "width": "9",
        "collapse": true
    },
    {
        "id": "15dfde8f.cb6381",
        "type": "ui_group",
        "z": "a2006c93.7ceb8",
        "name": "Map",
        "tab": "f783fcbb.2d6e2",
        "order": 1,
        "disp": true,
        "width": "12",
        "collapse": true
    },
    {
        "id": "3aad0b9d.3d01d4",
        "type": "ui_group",
        "z": "a2006c93.7ceb8",
        "name": "log (prel. data)",
        "tab": "f783fcbb.2d6e2",
        "order": 3,
        "disp": true,
        "width": "4",
        "collapse": true
    },
    {
        "id": "51c2b92b.f9fb48",
        "type": "ui_group",
        "z": "a2006c93.7ceb8",
        "name": "Recent earthquakes (UOA, department of Geophysics-Geothermics feed)",
        "tab": "f783fcbb.2d6e2",
        "order": 7,
        "disp": true,
        "width": "24",
        "collapse": true
    },
    {
        "id": "37dcb89.ee55b48",
        "type": "ui_group",
        "z": "",
        "name": "legend",
        "tab": "f783fcbb.2d6e2",
        "order": 2,
        "disp": true,
        "width": "4",
        "collapse": true
    },
    {
        "id": "631e82d4.384a7c",
        "type": "ui_group",
        "z": "",
        "name": "Help",
        "tab": "1406b827.c99c58",
        "disp": true,
        "width": "20",
        "collapse": true
    },
    {
        "id": "997921e.ec465e",
        "type": "ui_group",
        "z": "",
        "name": "About",
        "tab": "1406b827.c99c58",
        "disp": true,
        "width": "20",
        "collapse": true
    },
    {
        "id": "82a00fb3.45dbb",
        "type": "ui_group",
        "z": "",
        "name": "PGA",
        "tab": "f783fcbb.2d6e2",
        "order": 4,
        "disp": true,
        "width": "4",
        "collapse": true
    },
    {
        "id": "f783fcbb.2d6e2",
        "type": "ui_tab",
        "z": "a2006c93.7ceb8",
        "name": "Stations Data",
        "icon": "dashboard",
        "order": 1
    },
    {
        "id": "1406b827.c99c58",
        "type": "ui_tab",
        "z": "",
        "name": "About",
        "icon": "dashboard",
        "order": 3
    }
]

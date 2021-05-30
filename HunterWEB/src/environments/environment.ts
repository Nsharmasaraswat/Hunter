function loadJSON(filePath) {
  const json = loadTextFileAjaxSync(filePath, "application/json");
  let config: any = JSON.parse(json);

  config['version'] = require('../../package.json').version;
  return config;
}

function loadTextFileAjaxSync(filePath, mimeType) {
  const xmlhttp = new XMLHttpRequest();
  xmlhttp.open("GET", filePath, false);
  if (mimeType != null) {
    if (xmlhttp.overrideMimeType) {
      xmlhttp.overrideMimeType(mimeType);
    }
  }
  xmlhttp.send();
  if (xmlhttp.status == 200) {
    return xmlhttp.responseText;
  }
  else {
    return null;
  }
}

export const environment = loadJSON('assets/config/config.json');
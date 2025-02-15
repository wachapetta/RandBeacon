$(document).ready(function() {
    getCurrent();
    getLastPulseReloadPage();
    setInterval(function(){ getCurrent(); }, 10000);
});

function getCurrent() {
    $.ajax({
        url: "/unicorn/beacon/2.0/current/",
        method: 'GET',
        dataType: 'json',
        contentType: 'application/json',
        success: function (data) {
            updateFields(data);
        },
        error: function (xhr, status) {
            alert("Ocorreu um erro.  Por favor, tente mais tarde")
        }
    });
}

function getLastPulseReloadPage() {
    $.ajax({
        url: "/unicorn/beacon/2.0/last",
        method: 'GET',
        dataType: 'json',
        contentType: 'application/json',
        success: function (data) {
            atualizarRecord(data);
        }
    });
}

function getLastPulse() {
    $.ajax({
        url: "/unicorn/beacon/2.0/last",
        method: 'GET',
        dataType: 'json',
        contentType: 'application/json',
        success: function (data) {
            atualizarRecord(data);
        },
        error: function (xhr, status) {
            alert(xhr.status + '-' +xhr.responseText);
        }
    });
}

function updateFields(data) {
    document.getElementById("spanStatus").textContent = data.status;
    document.getElementById("spanNextRunInMinutes").textContent = data.nextRunInMinutes;
    document.getElementById("spanStart").textContent = data.start;
    document.getElementById("spanEnd").textContent = data.end;
    document.getElementById("spanCurrentHash").textContent = data.currentHash;

    var x = document.getElementById("pNextSubmission");
    if (data.status === "RUNNING" || data.status === "OPEN"){
        x.style.display = "none";
    } else {
        x.style.display = "block";
    }

    var seedList = data.seedList;
    updateSeedTableCurrent(seedList)

}

function updateSeedTableCurrent(seedList) {
    var lista = '';
    seedList.forEach(function (seed) {
        // lista += '<tr><td style="word-break: break-word">' + seed.timestamp + '</td>';
        lista += '<tr>';
        lista += '<td style="word-break: break-word">' + seed.timeStamp + '</td>';
        lista += '<td style="word-break: break-word">' + seed.seed + '</td>';
        lista += '<td style="word-break: break-word">' + seed.description + '</td>';
        lista += '<td style="word-break: break-word">' + seed.uri + '</td>';
        lista += '<td style="word-break: break-word">' + seed.cumulativeHash + '</td>';
        lista += '</tr>';

    });

    $('#table_seed_current').html(lista);
}




function getFirstPulse() {
    $.ajax({
        url: "/unicorn/beacon/2.0/first",
        method: 'GET',
        dataType: 'json',
        contentType: 'application/json',
        success: function (data) {
            atualizarRecord(data);
        },
        error: function (xhr) {
            alert(xhr.status + '-' +xhr.responseText);
        }
    });
}

function getPreviousPulse() {
    $.ajax({
        url: "/unicorn/beacon/2.0/previous/" + new Date(document.getElementById("input-datetime").value).getTime() ,
        method: 'GET',
        dataType: 'json',
        contentType: 'application/json',
        success: function (data) {
            atualizarRecord(data);
        },
        error: function (xhr) {
            alert(xhr.status + '-' +xhr.responseText);
        }
    });
}

function getNextPulse() {
    $.ajax({
        url: "/unicorn/beacon/2.0/next/" + new Date(document.getElementById("input-datetime").value).getTime(),
        method: 'GET',
        dataType: 'json',
        contentType: 'application/json',
        success: function (data) {
            atualizarRecord(data);
        },
        error: function (xhr) {
            alert(xhr.status + '-' +xhr.responseText);
        }
    });
}


function atualizarRecord(data) {
    var pulse = data.pulse;

    var elementById = document.getElementById("input-datetime");
    elementById.value = pulse.timeStamp;

    var lista = '';

    lista += '<tr><td>Uri:</td>';
    lista += '<td style="word-break: break-word">' + pulse.uri + '</td></tr>';

    lista += '<tr><td>Version:</td>';
    lista += '<td>' + pulse.version + '</td></tr>';

    lista += '<tr><td>Period:</td>';
    lista += '<td>' + pulse.period + '</td></tr>';

    lista += '<tr><td>Certified Hash:</td>';
    lista += '<td style="word-break: break-word">' + pulse.certificateId + '</td></tr>';

    lista += '<tr><td>Pulse Index:</td>';
    lista += '<td>' + pulse.pulseIndex + '</td></tr>';

    lista += '<tr><td>Time:</td>';
    lista += '<td>' + pulse.timeStamp + '</td></tr>';

    lista += '<tr><td>External Source Id:</td>';
    lista += '<td style="word-break: break-word">' + pulse.external.sourceId + '</td></tr>';

    lista += '<tr><td>External Status Code:</td>';
    lista += '<td>' + pulse.external.statusCode + '</td></tr>';

    lista += '<tr><td>External Value:</td>';
    lista += '<td style="word-break: break-word">' + pulse.external.value + '</td></tr>';

    lista += '<tr><td>Combination:</td>';
    lista += '<td>' + pulse.combination + '</td></tr>';

    lista += '<tr><td>Vdf p:</td>';
    lista += '<td style="word-break: break-word">' + pulse.sloth.p + '</td></tr>';

    lista += '<tr><td>Vdf x:</td>';
    lista += '<td style="word-break: break-word">' + pulse.sloth.x + '</td></tr>';

    lista += '<tr><td>Vdf iterations:</td>';
    lista += '<td>' + pulse.sloth.iterations + '</td></tr>';

    lista += '<tr><td>Vdf y:</td>';
    lista += '<td style="word-break: break-word">' + pulse.sloth.y + '</td></tr>';

    lista += '<tr><td>Signature:</td>';
    lista += '<td style="word-break: break-word">' + pulse.signatureValue + '</td></tr>';

    lista += '<tr><td>Output Value :</td>';
    lista += '<td style="word-break: break-word">' + pulse.outputValue + '</td></tr>';

    $('#table_pulse').html(lista);

    updateSeedTable(pulse.seedList);
}

function updateSeedTable(seedList) {
    var lista = '';
    seedList.forEach(function (seed) {
        lista += '<tr>';
        lista += '<td style="word-break: break-word">' + seed.timeStamp + '</td>';
        lista += '<td style="word-break: break-word">' + seed.seed + '</td>';
        lista += '<td style="word-break: break-word">' + seed.description + '</td>';
        lista += '<td style="word-break: break-word">' + seed.uri + '</td>';
        lista += '<td style="word-break: break-word">' + seed.cumulativeHash + '</td>';
        lista += '</tr>';

    });

    $('#table_seed').html(lista);
}
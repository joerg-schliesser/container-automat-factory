/*
 * Copyright 2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

function startDfaEditor() {

    const statesTable = document.getElementById('statesTable');
    statesTable.addEventListener('click', function (event) {

        if (event.target.tagName === 'TD') {
            const row = event.target.parentElement;
            if (event.target.cellIndex === 2 || event.target.cellIndex === 3) {
                document.getElementById('stateName').value = row.cells[0].innerText;
                document.getElementById('stateDescription').value = row.cells[1].innerText;
                if (event.target.cellIndex === 3) {
                    row.remove();
                    const stateName = event.target.id.substring(0, event.target.id.length - 'Remove'.length);
                    updateSelectOptions('currentState', stateName, 'remove', '');
                    updateSelectOptions('subsequentState', stateName, 'remove', '');
                    updateSelectOptions('startState', stateName, 'remove', '');
                    updateSelectOptions('acceptStates', stateName, 'remove', '');
                    alignTableRows('statesTable');
                    checkTransitions();
                    updateDfaJson();
                }
            }
        }
    });

    const symbolsTable = document.getElementById('symbolsTable');
    symbolsTable.addEventListener('click', function (event) {

        if (event.target.tagName === 'TD') {
            const row = event.target.parentElement;
            if (event.target.cellIndex === 2 || event.target.cellIndex === 3) {
                document.getElementById('symbolCharacter').value = row.cells[0].innerText;
                document.getElementById('symbolDescription').value = row.cells[1].innerText;
                if (event.target.cellIndex === 3) {
                    row.remove();
                    const symbolCharacter = event.target.id.substring(0, event.target.id.length - 'Remove'.length);
                    updateSelectOptions('inputSymbol', symbolCharacter, 'remove', '');
                    alignTableRows('symbolsTable');
                    checkTransitions();
                    updateDfaJson();
                }
            }
        }
    });

    const transitionsTable = document.getElementById('transitionsTable');
    transitionsTable.addEventListener('click', function (event) {

        if (event.target.tagName === 'TD') {
            const row = event.target.parentElement;
            if (event.target.tagName === 'TD' && event.target.cellIndex === 4 || event.target.cellIndex === 5) {
                document.getElementById('currentState').value = row.cells[0].innerText;
                document.getElementById('inputSymbol').value = row.cells[1].innerText;
                document.getElementById('subsequentState').value = row.cells[2].innerText;
                document.getElementById('transitionDescription').value = row.cells[3].innerText;
                if (event.target.cellIndex === 5) {
                    row.remove();
                    event.target.parentElement.remove();
                    alignTableRows('transitionsTable');
                    updateDfaJson();
                }
            }
        }
    });

    const stateDescriptionInput = document.getElementById('dfaDescription');
    stateDescriptionInput.addEventListener('input', function() {
        updateDfaJson();
    });

    prepareNextState();
    prepareNextSymbol();
}



function addOrUpdateState() {

    const stateName = document.getElementById('stateName').value;
    const stateDescription = document.getElementById('stateDescription').value;

    const tableRows = document.getElementById('statesTable').getElementsByTagName('tbody')[0].rows;
    for (let row of tableRows) {
        if (row.cells[0].innerText === stateName) {
            updateStateRow(row, stateName, stateDescription);
            return;
        }
    }

    addState(stateName, stateDescription);
}

function addState(stateName, stateDescription) {

    dismissStatusMessage('stateMessage');
    if (!stateName || !stateDescription) {
        showStatusMessage('stateMessage', 'Please provide a name and a description for the state.')
        return;
    }
    if (stateName.length > 8) {
        showStatusMessage('stateMessage', 'The name of the state may be a maximum of 8 characters long.')
        return;
    }

    const newRow = document.createElement('tr');
    newRow.innerHTML = `
        <td>${stateName}</td>
        <td>${stateDescription}</td>
        <td class="dfaedit" id="${stateName}Edit">[edit]</td>
        <td class="dfaremove" id="${stateName}Remove">[x]</td>
    `;
    document.getElementById('statesTable').getElementsByTagName('tbody')[0].appendChild(newRow);
    updateSelectOptions('currentState', stateName, 'add', '');
    updateSelectOptions('subsequentState', stateName, 'add', '');
    updateSelectOptions('startState', stateName, 'add', stateDescription);
    updateSelectOptions('acceptStates', stateName, 'add', stateDescription);
    sortTableRows('statesTable');
    checkTransitions();
    prepareNextState();
    updateDfaJson();
}

function prepareNextState() {

    const nextState = getNextValue('statesTable', 'S', '0');
    if(!nextState) {
        return;
    }
    document.getElementById('stateName').value = 'S' + nextState;
    document.getElementById('stateDescription').value = 'State ' + nextState + '.';
}

function updateStateRow(row, stateName, stateDescription) {

    dismissStatusMessage('stateMessage');
    if (!stateDescription) {
        showStatusMessage('stateMessage', 'Please provide a description for the state.')
        return;
    }

    row.cells[1].innerText = stateDescription;
    updateSelectOptions('currentState', stateName, 'update', '');
    updateSelectOptions('subsequentState', stateName, 'update', '');
    updateSelectOptions('startState', stateName, 'update', stateDescription);
    updateSelectOptions('acceptStates', stateName, 'update', stateDescription);
    updateDfaJson();
}

function addOrUpdateSymbol() {

    const symbolCharacter = document.getElementById('symbolCharacter').value;
    const symbolDescription = document.getElementById('symbolDescription').value;

    const tableRows = document.getElementById('symbolsTable').getElementsByTagName('tbody')[0].rows;
    for (let row of tableRows) {
        if (row.cells[0].innerText === symbolCharacter) {
            updateSymbolRow(row, symbolCharacter, symbolDescription);
            return;
        }
    }

    addSymbol(symbolCharacter, symbolDescription);
}

function addSymbol(symbolCharacter, symbolDescription) {

    dismissStatusMessage('symbolMessage');
    if (!symbolCharacter || !symbolDescription) {
        showStatusMessage('symbolMessage', 'Please enter a character and a description for the symbol.')
        return;
    }
    if (symbolCharacter.length > 1) {
        showStatusMessage('symbolMessage', 'The symbol must be a single character.')
        return;
    }

    const newRow = document.createElement('tr');
    newRow.innerHTML = `
        <td>${symbolCharacter}</td>
        <td>${symbolDescription}</td>
        <td class="dfaedit" id="${symbolCharacter}Edit">[edit]</td>
        <td class="dfaremove" id="${symbolCharacter}Remove">[x]</td>
    `;
    document.getElementById('symbolsTable').getElementsByTagName('tbody')[0].appendChild(newRow);
    updateSelectOptions('inputSymbol', symbolCharacter, 'add', '');
    sortTableRows('symbolsTable');
    checkTransitions();
    prepareNextSymbol();
    updateDfaJson();
}

function prepareNextSymbol() {

    const nextSymbol = getNextValue('symbolsTable', '', 'a');
    if(!nextSymbol) {
        return;
    }
    document.getElementById('symbolCharacter').value = nextSymbol;
    document.getElementById('symbolDescription').value = 'Symbol ' + nextSymbol + '.';
}

function updateSymbolRow(row, symbolCharacter, symbolDescription) {

    dismissStatusMessage('symbolMessage');
    if (!symbolDescription) {
        showStatusMessage('symbolMessage', 'Please enter a character and a description for the symbol.')
        return;
    }

    row.cells[1].innerText = symbolDescription;
    updateSelectOptions('inputSymbol', symbolCharacter, 'update', '');
    updateDfaJson();
}

function addOrUpdateTransition() {

    const currentState = document.getElementById('currentState').value;
    const inputSymbol = document.getElementById('inputSymbol').value;
    const subsequentState = document.getElementById('subsequentState').value;
    const transitionDescription = document.getElementById('transitionDescription').value;

    const tableRows = document.getElementById('transitionsTable').getElementsByTagName('tbody')[0].rows;
    for (let row of tableRows) {
        if (row.cells[0].innerText === currentState && row.cells[1].innerText === inputSymbol) {
            updateTransitionRow(row, subsequentState, transitionDescription);
            return;
        }
    }

    addTransition(currentState, inputSymbol, subsequentState, transitionDescription);
}

function addTransition(currentState, inputSymbol, subsequentState, transitionDescription) {

    dismissStatusMessage('transitionMessage');
    if (!currentState || !inputSymbol || !subsequentState) {
        showStatusMessage('transitionMessage', 'Please select the initial state, input symbol, and subsequent state.')
        return;
    }

    const newRow = document.createElement('tr');
    newRow.innerHTML = `
        <td>${currentState}</td>
        <td>${inputSymbol}</td>
        <td>${subsequentState}</td>
        <td>${transitionDescription}</td>
        <td class="dfaedit" id="${currentState}_${inputSymbol}Edit">[edit]</td>
        <td class="dfaremove" id="${currentState}_${inputSymbol}Remove">[x]</td>
    `;
    document.getElementById('transitionsTable').getElementsByTagName('tbody')[0].appendChild(newRow);
    sortTableRows('transitionsTable');
    updateDfaJson();
}

function updateTransitionRow(row, subsequentState, transitionDescription) {

    dismissStatusMessage('transitionMessage');

    row.cells[2].innerText = subsequentState;
    row.cells[3].innerText = transitionDescription;
    sortTableRows('transitionsTable');
    updateDfaJson();
}

function checkTransitions() {

    const result = {
        missingInitialState: false,
        missingSubsequentState: false,
        missingInputSymbol: false
    };
    const states = getStates();
    const symbols = getSymnols();
    const tableRows = document.getElementById('transitionsTable').getElementsByTagName('tbody')[0].rows;
    for (let row of tableRows) {

        const initialState = row.cells[0].innerText;
        const initialStateExists = states.some(state => {
            return state.name == initialState;
        });
        const subsequentState = row.cells[2].innerText;
        const subsequentStateExists = states.some(state => {
            return state.name == subsequentState;
        });
        const inputSymbol = row.cells[1].innerText;
        const inputSymbolExists = symbols.some(symbol => {
            return symbol.symbol == inputSymbol;
        });

        if (initialStateExists) {
            row.cells[0].classList.remove('dfamissing');
        } else {
            result.missingInitialState = true;
            row.cells[0].classList.add('dfamissing');
        }
        if (subsequentStateExists) {
            row.cells[2].classList.remove('dfamissing');
        } else {
            result.missingSubsequentState = true;
            row.cells[2].classList.add('dfamissing');
        }
        if (inputSymbolExists) {
            row.cells[1].classList.remove('dfamissing');
        } else {
            result.missingInputSymbol = true;
            row.cells[1].classList.add('dfamissing');
        }
    }
    return result;
}

function getStates() {

    const states = [];
    const statesTableRows = document.getElementById('statesTable').getElementsByTagName('tbody')[0].rows;
    for (let row of statesTableRows) {
        states.push({
            name: row.cells[0].innerText,
            description: row.cells[1].innerText,
        });
    }
    return states;
}

function getSymnols() {

    const symbols = [];
    const symbolsTableRows = document.getElementById('symbolsTable').getElementsByTagName('tbody')[0].rows;
    for (let row of symbolsTableRows) {
        symbols.push({
            symbol: row.cells[0].innerText,
            description: row.cells[1].innerText
        });
    }
    return symbols;
}

function getTransitions() {

    const transitions = [];
    const transitionsTableRows = document.getElementById('transitionsTable').getElementsByTagName('tbody')[0].rows;
    for (let row of transitionsTableRows) {
        transitions.push({
            currentStateName: row.cells[0].innerText,
            inputSymbol: row.cells[1].innerText,
            subsequentStateName: row.cells[2].innerText,
            description: row.cells[3].innerText
        });
    }
    return transitions;
}

function updateSelectOptions(selectId, value, action, text) {

    const selectElement = document.getElementById(selectId);

    if (action === 'add') {
        const option = document.createElement('option');
        option.value = value;
        if(text) {
            option.text = value + ' - ' + text;
        } else {
            option.text = value;
        }
        option.class = 'dfaoption';
        selectElement.add(option);
    } else if (action === 'update') {
        for (let i = 0; i < selectElement.options.length; i++) {
            if (selectElement.options[i].value === value) {
                if(text) {
                    selectElement.options[i].text = value + ' - ' + text;
                } else {
                    selectElement.options[i].text = value;
                }
                return;
            }
        }
    } else if (action === 'remove') {
        for (let i = 0; i < selectElement.options.length; i++) {
            if (selectElement.options[i].value === value) {
                selectElement.remove(i);
                return;
            }
        }
    } else if (action === 'clear') {
       while (selectElement.options.length > 0) {
           selectElement.remove(0);
       }
    }
}

function sortTableRows(tableId) {

    const tableRows = document.getElementById(tableId).getElementsByTagName('tbody')[0].rows;
    const sortedRows = Array.from(tableRows).sort(function (a, b) {
        const result = a.cells[0].innerText.localeCompare(b.cells[0].innerText);
        if (result === 0) {
            return a.cells[1].innerText.localeCompare(b.cells[1].innerText);
        }
        return result;
    });

    const tableBody = document.getElementById(tableId).getElementsByTagName('tbody')[0];
    tableBody.innerHTML = '';

    for (let row of sortedRows) {
        tableBody.appendChild(row);
    }
    alignTableRows(tableId);
}

function alignTableRows(tableId) {

    var table = document.getElementById(tableId);

    var bodyRow = table.querySelector('tbody tr:first-child');
    if (bodyRow == null) {
        return;
    }
    var bodyCells = bodyRow.children;

    var bodyWidths = Array.from(bodyCells).map(function(cell) {
        return cell.offsetWidth - 18;
    });

    var headCells = table.querySelector('thead tr').children;
    var headWidths = Array.from(headCells).map(function(cell) {
        return cell.offsetWidth - 18;
    });

    Array.from(headCells).forEach(function(cell, i) {
        if (headWidths[i] > bodyWidths[i]) {
            cell.style.width = (2 + headWidths[i]) + 'px';
        } else {
            cell.style.width = (2 + bodyWidths[i]) + 'px';
        }
    });

    Array.from(bodyCells).forEach(function(cell, i) {
        if (headWidths[i] > bodyWidths[i]) {
            cell.style.width = (2 + headWidths[i]) + 'px';
        } else {
            cell.style.width = (2 + bodyWidths[i]) + 'px';
        }
    });
};

function getNextValue(tableId, namePrefix, startValue) {

    const tableRows = document.getElementById(tableId).getElementsByTagName('tbody')[0].rows;
    let nextValue = startValue;
    for(;;) {
        let exists = false;
        for (let row of tableRows) {
            if (row.cells[0].innerText === namePrefix + nextValue) {
                exists = true;
                break;
            }
        }
        if (!exists) {
            break;
        }
        nextChar = String.fromCharCode(nextValue.charCodeAt(0) + 1);
        if (isCharCodeDigitOrLowercaseLetter(nextChar.charCodeAt(0))) {
            nextValue = nextChar;
        } else {
            return '';
        }
    }
    return nextValue;
}

function isCharCodeDigitOrLowercaseLetter(charCode) {

    if ((charCode >= 48 && charCode <= 57) || (charCode >= 97 && charCode <= 122)) {
        return true;
    }
    return false;
}

function showStatusMessage(messageElement, messageText) {

    document.getElementById(messageElement + 'Text').innerText = messageText;
    document.getElementById(messageElement).hidden = false;
}

function dismissStatusMessage(messageElement) {

    document.getElementById(messageElement + 'Text').innerText = '';
    document.getElementById(messageElement).hidden = true;
}

function recreateDfaJson() {

    dismissStatusMessage('dfaMessage');

    const states = getStates();
    if (states.length === 0) {
        showStatusMessage('dfaMessage', 'At least one state must be specified.');
        return;
    }

    const symbols = getSymnols();
    if (symbols.length === 0) {
        showStatusMessage('dfaMessage', 'At least one symbol must be specified.');
        return;
    }

    const transitions = getTransitions();
    if (transitions.length === 0) {
        showStatusMessage('dfaMessage', 'At least one state transition must be specified.');
        return;
    }

    const startState = document.getElementById('startState').value;
    if (!startState) {
        showStatusMessage('dfaMessage', 'A start state must be specified.');
        return;
    }

    const acceptStates = Array.from(document.getElementById('acceptStates').options)
        .filter(option => option.selected)
        .map(option => option.value);
    if (acceptStates.length === 0) {
        showStatusMessage('dfaMessage', 'At least one accepting state must be specified.');
        return;
    }

    const validationMessage = validateDfa();
    if (validationMessage) {
        showStatusMessage('dfaMessage', validationMessage);
    }

    updateDfaJson();
}

function updateDfaJson() {

    dismissStatusMessage('dfaMessage');

    const states = getStates();
    const symbols = getSymnols();
    const transitions = getTransitions();
    const startState = document.getElementById('startState').value;
    const acceptStates = Array.from(document.getElementById('acceptStates').options)
        .filter(option => option.selected)
        .map(option => option.value);
    const description = document.getElementById('dfaDescription').value;

    const dfa = {
        alphabet: symbols,
        states: states,
        transitions: transitions,
        startState: startState,
        acceptStates: acceptStates,
        description: description
    };

    const dfaJson = JSON.stringify(dfa, null, 2);
    document.getElementById('output').value = dfaJson;
}

function initializeEditorValues() {

    dismissStatusMessage('dfaMessage');
    clearDfa();
    const dfaJson = document.getElementById('output').value;
    const dfa = JSON.parse(dfaJson);

    const states = dfa.states;
    for (let state of states) {
        const newRow = document.createElement('tr');
        newRow.innerHTML = `
            <td>${state.name}</td>
            <td>${state.description}</td>
            <td class="dfaedit" id="${state.name}Edit">[edit]</td>
            <td class="dfaremove" id="${state.name}Remove">[x]</td>
        `;
        document.getElementById('statesTable').getElementsByTagName('tbody')[0].appendChild(newRow);
        updateSelectOptions('currentState', state.name, 'add', '');
        updateSelectOptions('subsequentState', state.name, 'add', '');
        updateSelectOptions('startState', state.name, 'add', state.description);
        updateSelectOptions('acceptStates', state.name, 'add', state.description);
    }
    sortTableRows('statesTable');

    const symbols = dfa.alphabet;
    for (let symbol of symbols) {
        const newRow = document.createElement('tr');
        newRow.innerHTML = `
            <td>${symbol.symbol}</td>
            <td>${symbol.description}</td>
            <td class="dfaedit" id="${symbol.symbol}Edit">[edit]</td>
            <td class="dfaremove" id="${symbol.symbol}Remove">[x]</td>
        `;
        document.getElementById('symbolsTable').getElementsByTagName('tbody')[0].appendChild(newRow);
        updateSelectOptions('inputSymbol', symbol.symbol, 'add', '');
    }
    sortTableRows('symbolsTable');

    const transitions = dfa.transitions;
    for (let transition of transitions) {
        const newRow = document.createElement('tr');
        newRow.innerHTML = `
            <td>${transition.currentStateName}</td>
            <td>${transition.inputSymbol}</td>
            <td>${transition.subsequentStateName}</td>
            <td>${transition.description}</td>
            <td class="dfaedit" id="${transition.currentStateName}_${transition.inputSymbol}Edit">[edit]</td>
            <td class="dfaremove" id="${transition.currentStateName}_${transition.inputSymbol}Remove">[x]</td>
        `;
        document.getElementById('transitionsTable').getElementsByTagName('tbody')[0].appendChild(newRow);
    }
    sortTableRows('transitionsTable');
    checkTransitions();
    const validationMessage = validateDfa();
    if (validationMessage) {
        showStatusMessage('dfaMessage', validationMessage);
    }

    const acceptStates = Array.from(document.getElementById('acceptStates').options)
    for (let acceptStateOption of acceptStates) {
        acceptStateOption.selected = dfa.acceptStates.some(acceptState => {
            return acceptState === acceptStateOption.value;
        });
    }
    document.getElementById('startState').value = dfa.startState;
    document.getElementById('dfaDescription').value = dfa.description;
}

function validateDfa() {

    const ambiguousTransitions = checkTransitions();
    var ambiguousTransitionsMessage = '';
    if (ambiguousTransitions.missingInitialState && ambiguousTransitions.missingSubsequentState) {
        ambiguousTransitionsMessage = 'There are transitions with invalid initial and subsequent state';
    } else if (ambiguousTransitions.missingInitialState) {
        ambiguousTransitionsMessage = 'There are transitions with invalid initial state';
    } else if (ambiguousTransitions.missingSubsequentState) {
        ambiguousTransitionsMessage = 'There are transitions with invalid subsequent state';
    }
    if (ambiguousTransitions.missingInputSymbol) {
        if (ambiguousTransitionsMessage) {
            ambiguousTransitionsMessage += ' and invalid input symbol';
        } else {
            ambiguousTransitionsMessage = 'There are transitions with invalid input symbol';
        }
    }
    return ambiguousTransitionsMessage;
}

function resetDfa() {

    clearDfa();
    document.getElementById('output').value = '';
    document.getElementById('dfaDescription').value = 'Deterministic Finite Automaton.';
}

function clearDfa() {

    const statesTableRows = document.getElementById('statesTable').getElementsByTagName('tbody')[0].rows;
    for (let i = statesTableRows.length - 1; i >= 0; i--) {
        statesTableRows[i].remove();
    }
    updateSelectOptions('currentState', '', 'clear', '');
    updateSelectOptions('subsequentState', '', 'clear', '');
    updateSelectOptions('startState', '', 'clear', '');
    updateSelectOptions('acceptStates', '', 'clear', '');

    const symbolsTableRows = document.getElementById('symbolsTable').getElementsByTagName('tbody')[0].rows;
    for (let i = symbolsTableRows.length - 1; i >= 0; i--) {
        symbolsTableRows[i].remove();
    }
    updateSelectOptions('inputSymbol', '', 'clear', '');

    const transitionsTableRows = document.getElementById('transitionsTable').getElementsByTagName('tbody')[0].rows;
    for (let i = transitionsTableRows.length - 1; i >= 0; i--) {
        transitionsTableRows[i].remove();
    }

    const acceptStates = Array.from(document.getElementById('acceptStates').options)
    for (let acceptStateOption of acceptStates) {
        acceptStateOption.selected = false;
    }

    document.getElementById('startState').value = '';
    document.getElementById('dfaDescription').value = '';
}

function initSampleDfa() {

    const sampleDfa = document.getElementById('sampleDfa').value;
    if (sampleDfa === 'beverageVending') {
        document.getElementById('output').value = beverageVendingJson;
        document.getElementById('appName').value = 'BeverageVending';
        document.getElementById('appPackage').value = 'samples.beveragevending';
    } else if (sampleDfa === 'decimalNumbersCheck') {
        document.getElementById('output').value = decimalNumbersCheckJson;
        document.getElementById('appName').value = 'DecimalNumbersCheck';
        document.getElementById('appPackage').value = 'samples.decimalnumbers';
    } else if (sampleDfa === 'evenZerosCheck') {
        document.getElementById('output').value = evenZerosCheckJson;
        document.getElementById('appName').value = 'EvenZerosCheck';
        document.getElementById('appPackage').value = 'samples.evenzeros';
    }
    initializeEditorValues();
}

function createContainerAutomatApp() {

    const url = "/v1/apps/create";
    const xhr = new XMLHttpRequest();
    const appName = document.getElementById('appName').value.trim();
    const containerRegistry = document.getElementById('containerRegistry').value.trim();
    const appPackage = document.getElementById('appPackage').value.trim();
    const messagingType = document.getElementById('messagingType').value;
    const storageType = document.getElementById('storageType').value;
    const includeOptionalServices = document.getElementById('includeOptionalServices').checked;
    var dfa;

    if (!appName) {
        showStatusMessage('appMessage', 'The application name must not be empty.')
        return;
    }
    if (appName.indexOf(' ') !== -1) {
        showStatusMessage('appMessage', 'The application name must not contain any spaces.')
        return;
    }
    if (containerRegistry.indexOf(' ') !== -1) {
        showStatusMessage('appMessage', 'The name of the container registry must not contain any spaces.')
        return;
    }

    const validationMessage = validateDfa();
    if (validationMessage) {
        showStatusMessage('appMessage', validationMessage);
        return;
    }
    const dfaJson = document.getElementById('output').value.trim();
    if (!dfaJson) {
        showStatusMessage('appMessage', 'The DFA specification must not be empty.')
        return;
    }

    try {
        dfa = JSON.parse(dfaJson);
    } catch (error) {
        showStatusMessage('appMessage', 'Unable to create DFA. ' + error)
        return;
    }

    xhr.open("POST", url, true);
    xhr.setRequestHeader("Content-Type", "application/json");
    xhr.responseType = 'blob';
    xhr.onreadystatechange = function () {
        if (xhr.readyState !== 4) {
            return;
        }
        if (xhr.status === 200) {
            const blob = new Blob([xhr.response], {type: 'application/zip'});
            const link = document.createElement('a');
            link.href = window.URL.createObjectURL(blob);
            link.download = appName.toLowerCase() + '.zip';
            link.click();
        } else {
            const fileReader = new FileReader();
            fileReader.onload = function(e) {
                showStatusMessage('appMessage', 'Error, Status: ' + xhr.status + '\n\n' + e.target.result);
            }
            fileReader.readAsText(xhr.response);
        }
    };

    const metaData = new Object();
    metaData['appName'] = appName;
    metaData['appPackage'] = appPackage;
    metaData['containerRegistry'] = containerRegistry;
    metaData['messagingType'] = messagingType;
    metaData['storageType'] = storageType;
    metaData['includeOptionalServices'] = includeOptionalServices;

    const generationParameters = new Object();
    generationParameters['dfa'] = dfa;
    generationParameters['applicationMetaData'] = metaData;
    const parametersJson = JSON.stringify(generationParameters, null, 2);
    xhr.send(parametersJson);
}

const evenZerosCheckJson =
`{
  "alphabet": [
    {
      "symbol": "0",
      "description": "The 0 symbol."
    },
    {
      "symbol": "1",
      "description": "The 1 symbol."
    }
  ],
  "states": [
    {
      "name": "S1",
      "description": "Even number of zeros read."
    },
    {
      "name": "S2",
      "description": "Odd number of zeros read."
    }
  ],
  "transitions": [
    {
      "currentStateName": "S1",
      "inputSymbol": "0",
      "subsequentStateName": "S2",
      "description": "Input of symbol 0."
    },
    {
      "currentStateName": "S1",
      "inputSymbol": "1",
      "subsequentStateName": "S1",
      "description": "Input of symbol 1."
    },
    {
      "currentStateName": "S2",
      "inputSymbol": "0",
      "subsequentStateName": "S1",
      "description": "Input of symbol 0."
    },
    {
      "currentStateName": "S2",
      "inputSymbol": "1",
      "subsequentStateName": "S2",
      "description": "Input of symbol 1."
    }
  ],
  "startState": "S1",
  "acceptStates": [
    "S1"
  ],
  "description": "A DFA for checking the input of an even number of zeros."
}`;

const beverageVendingJson =
`{
  "alphabet": [
    {
      "symbol": "A",
      "description": "Abort."
    },
    {
      "symbol": "P",
      "description": "Payment."
    },
    {
      "symbol": "R",
      "description": "Removal of beverage."
    },
    {
      "symbol": "S",
      "description": "Selection of beverage."
    }
  ],
  "states": [
    {
      "name": "S1",
      "description": "Ready for beverage selection."
    },
    {
      "name": "S2",
      "description": "Waiting for payment."
    },
    {
      "name": "S3",
      "description": "Waiting for beverage removal."
    }
  ],
  "transitions": [
    {
      "currentStateName": "S1",
      "inputSymbol": "S",
      "subsequentStateName": "S2",
      "description": "Beverage selected."
    },
    {
      "currentStateName": "S2",
      "inputSymbol": "A",
      "subsequentStateName": "S1",
      "description": "Purchase aborted."
    },
    {
      "currentStateName": "S2",
      "inputSymbol": "P",
      "subsequentStateName": "S3",
      "description": "Payment made."
    },
    {
      "currentStateName": "S3",
      "inputSymbol": "R",
      "subsequentStateName": "S1",
      "description": "Beverage removed."
    }
  ],
  "startState": "S1",
  "acceptStates": [
    "S1"
  ],
  "description": "A DFA of a beverage vending machine."
}`;

const decimalNumbersCheckJson =
`{
  "alphabet": [
    {
      "symbol": "-",
      "description": "Minus."
    },
    {
      "symbol": ".",
      "description": "Decimal point."
    },
    {
      "symbol": "+",
      "description": "Plus."
    },
    {
      "symbol": "0",
      "description": "Digit 0."
    },
    {
      "symbol": "1",
      "description": "Digit 1."
    },
    {
      "symbol": "2",
      "description": "Digit 2."
    },
    {
      "symbol": "3",
      "description": "Digit 3."
    },
    {
      "symbol": "4",
      "description": "Digit 4."
    },
    {
      "symbol": "5",
      "description": "Digit 5."
    },
    {
      "symbol": "6",
      "description": "Digit 6."
    },
    {
      "symbol": "7",
      "description": "Digit 7."
    },
    {
      "symbol": "8",
      "description": "Digit 8."
    },
    {
      "symbol": "9",
      "description": "Digit 9."
    }
  ],
  "states": [
    {
      "name": "S0",
      "description": "Start state."
    },
    {
      "name": "S1",
      "description": "After sign."
    },
    {
      "name": "S2",
      "description": "After integer part 0."
    },
    {
      "name": "S3",
      "description": "After digit in the integer part."
    },
    {
      "name": "S4",
      "description": "After decimal point."
    },
    {
      "name": "S5",
      "description": "After digit in decimal part."
    }
  ],
  "transitions": [
    {
      "currentStateName": "S0",
      "inputSymbol": "-",
      "subsequentStateName": "S1",
      "description": "Negative sign read."
    },
    {
      "currentStateName": "S0",
      "inputSymbol": "+",
      "subsequentStateName": "S1",
      "description": "Positive sign read."
    },
    {
      "currentStateName": "S0",
      "inputSymbol": "0",
      "subsequentStateName": "S2",
      "description": "Leading digit 0 read."
    },
    {
      "currentStateName": "S0",
      "inputSymbol": "1",
      "subsequentStateName": "S3",
      "description": "Leading digit 1 read."
    },
    {
      "currentStateName": "S0",
      "inputSymbol": "2",
      "subsequentStateName": "S3",
      "description": "Leading digit 2 read."
    },
    {
      "currentStateName": "S0",
      "inputSymbol": "3",
      "subsequentStateName": "S3",
      "description": "Leading digit 3 read."
    },
    {
      "currentStateName": "S0",
      "inputSymbol": "4",
      "subsequentStateName": "S3",
      "description": "Leading digit 4 read."
    },
    {
      "currentStateName": "S0",
      "inputSymbol": "5",
      "subsequentStateName": "S3",
      "description": "Leading digit 5 read."
    },
    {
      "currentStateName": "S0",
      "inputSymbol": "6",
      "subsequentStateName": "S3",
      "description": "Leading digit 6 read."
    },
    {
      "currentStateName": "S0",
      "inputSymbol": "7",
      "subsequentStateName": "S3",
      "description": "Leading digit 7 read."
    },
    {
      "currentStateName": "S0",
      "inputSymbol": "8",
      "subsequentStateName": "S3",
      "description": "Leading digit 8 read."
    },
    {
      "currentStateName": "S0",
      "inputSymbol": "9",
      "subsequentStateName": "S3",
      "description": "Leading digit 9 read."
    },
    {
      "currentStateName": "S1",
      "inputSymbol": "0",
      "subsequentStateName": "S2",
      "description": "Digit 0 read in integer part."
    },
    {
      "currentStateName": "S1",
      "inputSymbol": "1",
      "subsequentStateName": "S3",
      "description": "Digit 1 read in integer part."
    },
    {
      "currentStateName": "S1",
      "inputSymbol": "2",
      "subsequentStateName": "S3",
      "description": "Digit 2 read in integer part."
    },
    {
      "currentStateName": "S1",
      "inputSymbol": "3",
      "subsequentStateName": "S3",
      "description": "Digit 3 read in integer part."
    },
    {
      "currentStateName": "S1",
      "inputSymbol": "4",
      "subsequentStateName": "S3",
      "description": "Digit 4 read in integer part."
    },
    {
      "currentStateName": "S1",
      "inputSymbol": "5",
      "subsequentStateName": "S3",
      "description": "Digit 5 read in integer part."
    },
    {
      "currentStateName": "S1",
      "inputSymbol": "6",
      "subsequentStateName": "S3",
      "description": "Digit 6 read in integer part."
    },
    {
      "currentStateName": "S1",
      "inputSymbol": "7",
      "subsequentStateName": "S3",
      "description": "Digit 7 read in integer part."
    },
    {
      "currentStateName": "S1",
      "inputSymbol": "8",
      "subsequentStateName": "S3",
      "description": "Digit 8 read in integer part."
    },
    {
      "currentStateName": "S1",
      "inputSymbol": "9",
      "subsequentStateName": "S3",
      "description": "Digit 9 read in integer part."
    },
    {
      "currentStateName": "S2",
      "inputSymbol": ".",
      "subsequentStateName": "S4",
      "description": "Decimal point read."
    },
    {
      "currentStateName": "S3",
      "inputSymbol": ".",
      "subsequentStateName": "S4",
      "description": "Decimal point read."
    },
    {
      "currentStateName": "S3",
      "inputSymbol": "0",
      "subsequentStateName": "S3",
      "description": "Digit 0 read in integer part."
    },
    {
      "currentStateName": "S3",
      "inputSymbol": "1",
      "subsequentStateName": "S3",
      "description": "Digit 1 read in integer part."
    },
    {
      "currentStateName": "S3",
      "inputSymbol": "2",
      "subsequentStateName": "S3",
      "description": "Digit 2 read in integer part."
    },
    {
      "currentStateName": "S3",
      "inputSymbol": "3",
      "subsequentStateName": "S3",
      "description": "Digit 3 read in integer part."
    },
    {
      "currentStateName": "S3",
      "inputSymbol": "4",
      "subsequentStateName": "S3",
      "description": "Digit 4 read in integer part."
    },
    {
      "currentStateName": "S3",
      "inputSymbol": "5",
      "subsequentStateName": "S3",
      "description": "Digit 5 read in integer part."
    },
    {
      "currentStateName": "S3",
      "inputSymbol": "6",
      "subsequentStateName": "S3",
      "description": "Digit 6 read in integer part."
    },
    {
      "currentStateName": "S3",
      "inputSymbol": "7",
      "subsequentStateName": "S3",
      "description": "Digit 7 read in integer part."
    },
    {
      "currentStateName": "S3",
      "inputSymbol": "8",
      "subsequentStateName": "S3",
      "description": "Digit 8 read in integer part."
    },
    {
      "currentStateName": "S3",
      "inputSymbol": "9",
      "subsequentStateName": "S3",
      "description": "Digit 9 read in integer part."
    },
    {
      "currentStateName": "S4",
      "inputSymbol": "0",
      "subsequentStateName": "S5",
      "description": "0 is read as the first digit of the decimal part."
    },
    {
      "currentStateName": "S4",
      "inputSymbol": "1",
      "subsequentStateName": "S5",
      "description": "1 is read as the first digit of the decimal part."
    },
    {
      "currentStateName": "S4",
      "inputSymbol": "2",
      "subsequentStateName": "S5",
      "description": "2 is read as the first digit of the decimal part."
    },
    {
      "currentStateName": "S4",
      "inputSymbol": "3",
      "subsequentStateName": "S5",
      "description": "3 is read as the first digit of the decimal part."
    },
    {
      "currentStateName": "S4",
      "inputSymbol": "4",
      "subsequentStateName": "S5",
      "description": "4 is read as the first digit of the decimal part."
    },
    {
      "currentStateName": "S4",
      "inputSymbol": "5",
      "subsequentStateName": "S5",
      "description": "5 is read as the first digit of the decimal part."
    },
    {
      "currentStateName": "S4",
      "inputSymbol": "6",
      "subsequentStateName": "S5",
      "description": "6 is read as the first digit of the decimal part."
    },
    {
      "currentStateName": "S4",
      "inputSymbol": "7",
      "subsequentStateName": "S5",
      "description": "7 is read as the first digit of the decimal part."
    },
    {
      "currentStateName": "S4",
      "inputSymbol": "8",
      "subsequentStateName": "S5",
      "description": "8 is read as the first digit of the decimal part."
    },
    {
      "currentStateName": "S4",
      "inputSymbol": "9",
      "subsequentStateName": "S5",
      "description": "9 is read as the first digit of the decimal part."
    },
    {
      "currentStateName": "S5",
      "inputSymbol": "0",
      "subsequentStateName": "S5",
      "description": "Digit 0 read in decimal part."
    },
    {
      "currentStateName": "S5",
      "inputSymbol": "1",
      "subsequentStateName": "S5",
      "description": "Digit 1 read in decimal part."
    },
    {
      "currentStateName": "S5",
      "inputSymbol": "2",
      "subsequentStateName": "S5",
      "description": "Digit 2 read in decimal part."
    },
    {
      "currentStateName": "S5",
      "inputSymbol": "3",
      "subsequentStateName": "S5",
      "description": "Digit 3 read in decimal part."
    },
    {
      "currentStateName": "S5",
      "inputSymbol": "4",
      "subsequentStateName": "S5",
      "description": "Digit 4 read in decimal part."
    },
    {
      "currentStateName": "S5",
      "inputSymbol": "5",
      "subsequentStateName": "S5",
      "description": "Digit 5 read in decimal part."
    },
    {
      "currentStateName": "S5",
      "inputSymbol": "6",
      "subsequentStateName": "S5",
      "description": "Digit 6 read in decimal part."
    },
    {
      "currentStateName": "S5",
      "inputSymbol": "7",
      "subsequentStateName": "S5",
      "description": "Digit 7 read in decimal part."
    },
    {
      "currentStateName": "S5",
      "inputSymbol": "8",
      "subsequentStateName": "S5",
      "description": "Digit 8 read in decimal part."
    },
    {
      "currentStateName": "S5",
      "inputSymbol": "9",
      "subsequentStateName": "S5",
      "description": "Digit 9 read in decimal part."
    }
  ],
  "startState": "S0",
  "acceptStates": [
    "S2",
    "S3",
    "S5"
  ],
  "description": "A DFA for the recognition of decimal numbers."
}`;

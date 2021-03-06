import { ActionTree } from 'vuex';
import axios, { AxiosPromise } from 'axios';
import { CheckResultState, CheckResult, FileCheckResult, UserLoginInfo, Pair, CheckResultType, CheckCategory, IoLine } from '../../types';
import { RootState } from '../../types';

/**
 * Parses a response the server sent for a check.
 * 
 * @param json the json to parse
 * @return the parsed CheckResult
 */
function parseCheckResponse(json: any): CheckResult {

  if (json.diagnostics) {
    return parseCompilationOutput(json)
  }

  const entries = new Array<Pair<string, Array<FileCheckResult>>>()
  Object.keys(json.fileResults).forEach(fileName => {
    const checkResults = json.fileResults[fileName].map((json: any) => {
      return new FileCheckResult(json.check, json.result, json.message, json.errorOutput, parseLines(json.output))
    })
    entries.push(new Pair(fileName, checkResults));
  })

  return new CheckResult(entries)
}

function parseLines(lines: Array<any>): Array<IoLine> {
  return lines.map(line => {
    return new IoLine(line.type.toLowerCase(), line.content);
  })
}

/**
 * Parses a compilation output response from the given json.
 * 
 * @param json the json to parse
 * @return the parsed CheckResult
 */
function parseCompilationOutput(json: any): CheckResult {
  const entries = new Array<Pair<string, Array<FileCheckResult>>>()

  Object.keys(json.diagnostics).forEach(fileName => {
    const diagnostics = json.diagnostics[fileName] as Array<string>
    const convertedToCheckResults = diagnostics
      .map(diagString => new FileCheckResult("Compilation", CheckResultType.FAILED, diagString, "", []))

    entries.push(new Pair(fileName, convertedToCheckResults));
  })

  if (json.output && json.output !== "") {
    const checkResult = new FileCheckResult("Compilation", CheckResultType.FAILED, json.output, "", []);
    entries.push(new Pair("N/A", [checkResult]));
  }

  return new CheckResult(entries);
}

export const actions: ActionTree<CheckResultState, RootState> = {
  checkSingle({ commit, state }, payload: Pair<CheckCategory, string>): AxiosPromise<any> {
    return axios.post(`/test/single/${payload.key.id}`, payload.value, {
      headers: { "Content-Type": "text/plain" }
    }).then(response => {
      commit("checkResult", parseCheckResponse(response.data))

      return response
    });
  },
  checkMultiple({ commit, state }, payload: Pair<CheckCategory, Array<File>>): AxiosPromise<any> {
    const formData = new FormData()
    payload.value.forEach(file => {
      formData.append(file.name, file)
    })
    return axios.post(`/test/multiple/${payload.key.id}`, formData)
      .then(response => {
        commit("checkResult", parseCheckResponse(response.data))

        return response
      })
  },
  checkZip({ commit, state }, payload: Pair<CheckCategory, File>): AxiosPromise<any> {
    const formData = new FormData()
    formData.append("file", payload.value)
    return axios.post(`/test/zip/${payload.key.id}`, formData)
      .then(response => {
        commit("checkResult", parseCheckResponse(response.data))

        return response
      })
  }

};
fake_encrypted = "abcdefghijklmnopqrstuvwxyz"


valid_id_tag = '{"tx_id": "0f534ffc-9442-414c-b39f-a756b4adc6cb","is_feedback":false}'

valid_decrypted = '''
{
  "tx_id": "0f534ffc-9442-414c-b39f-a756b4adc6cb",
  "type": "uk.gov.ons.edc.eq:surveyresponse",
  "version": "0.0.1",
  "origin": "uk.gov.ons.edc.eq",
  "survey_id": "023",
  "case_id": "58506af6-b484-48e9-bda7-3e4e24205545",
  "collection": {
    "exercise_sid": "hfjdskf",
    "instrument_id": "0102",
    "period": "1604"
  },
  "metadata": {
    "user_id": "789473423",
    "ru_ref": "12345678901A"
  },
  "data": {
    "11": "1/4/2016",
    "12": "31/10/2016",
    "20": "1800000",
    "21": "60000"
  }
}
'''

valid_rm_decrypted = '''
{
  "tx_id": "0f534ffc-9442-414c-b39f-a756b4adc6cb",
  "type": "uk.gov.ons.edc.eq:surveyresponse",
  "version": "0.0.1",
  "origin": "uk.gov.ons.edc.eq",
  "survey_id": "023",
  "case_id": "58506af6-b484-48e9-bda7-3e4e24205545",
  "collection": {
    "exercise_sid": "hfjdskf",
    "instrument_id": "0102",
    "period": "1604"
  },
  "metadata": {
    "user_id": "789473423",
    "ru_ref": "12345678901A"
  },
  "data": {
    "11": "1/4/2016",
    "12": "31/10/2016",
    "20": "1800000",
    "21": "60000"
  }
}
'''

invalid_decrypted = '''
{
  "tx_id": "0f534ffc-9442-414c-b39f-a756b4adc6cb",
  "type": "uk.gov.ons.edc.eq:surveyresponse",
  "version": "0.0.1",
  "origin": "uk.gov.ons.edc.eq",
  "survey_id": 1,
  "case_id": "58506af6-b484-48e9-bda7-3e4e24205545",
  "collection": {
    "exercise_sid": "hfjdskf",
    "instrument_id": "0102",
    "period": "1604"
  },
  "metadata": {
    "user_id": "789473423",
    "ru_ref": "12345678901A"
  },
  "data": {
    "11": "1/4/2016",
    "12": "31/10/2016",
    "20": "1800000",
    "21": "60000"
  }
}
'''

feedback_decrypted = '''
{
  "tx_id": "0f534ffc-9442-414c-b39f-a756b4adc6cb",
  "type": "uk.gov.ons.edc.eq:feedback",
  "version": "0.0.1",
  "origin": "uk.gov.ons.edc.eq",
  "survey_id": "023",
  "collection": {
    "exercise_sid": "hfjdskf",
    "instrument_id": "0102",
    "period": "1604"
  },
  "metadata": {
    "user_id": "789473423",
    "ru_ref": "12345678901A"
  },
  "data": {
    "11": "1/4/2016",
    "12": "31/10/2016",
    "20": "1800000",
    "21": "60000"
  }
}
'''

valid_census_decrypted = '''
{
  "submitted_at": "2018-06-01T13:31:39.953654+00:00",
  "survey_id": "census",
  "case_ref": "1000000000000001",
  "data": [
    {
      "answer_instance": 0,
      "value": "Yes",
      "answer_id": "address-check-answer",
      "block_id": "address-check-block",
      "group_id": "about-household-group",
      "group_instance": 0
    }
  ],
  "version": "0.0.2",
  "metadata": {
    "ref_period_end_date": "2018-05-31",
    "ru_ref": "Test Data",
    "ref_period_start_date": "2018-05-01",
    "user_id": "UNKNOWN"
  },
  "origin": "uk.gov.ons.edc.eq",
  "collection": {
    "instrument_id": "1",
    "period": "201605",
    "exercise_sid": "789"
  },
  "type": "uk.gov.ons.edc.eq:surveyresponse",
  "tx_id": "9660044e-e756-4afd-bb23-a3cf39957ba1",
  "case_id": "4c0bc9ec-06d4-4f66-88b6-2e42b79e17b3"
}
'''

dap_manifest = '''
{
    "version":1,
    "files":[
        {
            "sizeBytes":7257,
            "md5sum":"9d7bf6f4dc2c2029bc12c9a60e2d9438",
            "name":"9660044e-e756-4afd-bb23-a3cf39957ba1.json",
            "URL":"http://sdx-store:5000/responses/9660044e-e756-4afd-bb23-a3cf39957ba1"
     }
    ],
    "sensitivity":"High",
    "sourceName":"<ENVIRONMENT>",
    "manifestCreated":"2018-09-19T13:56:59.753Z",
    "description":"census survey response for period 2021 sample unit 1",
    "iterationL1":"household",
    "dataset":"census",
    "schemaVersion":1
}
'''

fake_encrypted = "abcdefghijklmnopqrstuvwxyz"

valid_decrypted = '''
{
  "tx_id": "0f534ffc-9442-414c-b39f-a756b4adc6cb",
  "type": "uk.gov.ons.edc.eq:surveyresponse",
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

invalid_decrypted = '''
{
  "abc": "def"
}
'''

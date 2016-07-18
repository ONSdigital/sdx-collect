fake_encrypted = "abcdefghijklmnopqrstuvwxyz"

valid_decrypted = '''
{
  "type": "uk.gov.ons.edc.eq:surveyresponse",
  "origin": "uk.gov.ons.edc.eq",
  "survey_id": "194825",
  "version": "0.0.1",
  "collection": {
    "exercise_sid": "hfjdskf",
    "instrument_id": "10",
    "period": "0616"
  },
  "submitted_at": "2016-03-12T10:39:40Z",
  "metadata": {
    "user_id": "789473423",
    "ru_ref": "1234570071A"
  },
  "data": {
    "1": "2",
    "2": "4",
    "3": "2",
    "4": "Y"
  }
}
'''

invalid_decrypted = '''
{
  "abc": "def"
}
'''

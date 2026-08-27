# Model Cards

## Current Status

No ML models are included in this version of DailyTrack.

## Future ML Features

If ML models are added in the future, they will:

1. Be on-device only (no cloud inference)
2. Have permissive open-source licenses
3. Be disabled by default
4. Have documented model cards
5. Not be used for medical diagnosis
6. Not use face recognition or biometric identification

## Planned ML Features (Optional)

### Activity Suggestion
- Suggest likely category based on time and history
- User confirms suggestion
- Model: Simple classification

### Routine Anomaly Detection
- Detect unusual deviations from normal routine
- Show as suggestion, not fact
- Model: Statistical anomaly detection

### Food Photo Classification
- Only with permissively licensed model
- Result is suggestion, not guarantee
- Model: ImageNet-trained classifier (if bundled)

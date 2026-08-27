# Nutrition Data Sources

## Current Approach

DailyTrack uses **user-provided food entries only**. No nutrition database is bundled with the app.

## Future Options

If nutrition data lookup is added in the future:

### Open Food Facts (Recommended)
- **License:** Open Database License (ODbL)
- **URL:** https://world.openfoodfacts.org/
- **Usage:** Optional online lookup with user consent
- **Privacy:** No user ID sent, no precise location
- **Attribution:** Required by ODbL license

### CSV Import
- Users can import custom nutrition databases
- Format: CSV with nutrient columns
- No automatic download

## Rules

1. No proprietary nutrition databases
2. Online lookup disabled by default
3. User consent required for network features
4. Attribution provided for any data sources
5. App fully functional offline

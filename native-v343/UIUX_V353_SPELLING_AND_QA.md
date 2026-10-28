# Bagan Heritage Museum V3.5.3 — UI/UX Copy & Screen QA
Design reference: Google Drive folder 18hy4kto05d9Ns_CjojSz5zOz7Mic10c0 (20 concept screens).

## Approved screen titles (English / Myanmar Unicode)
01 Splash — ပုဂံအမွေအနှစ်ပြတိုက်
02 Onboarding — ပုဂံကို လေ့လာကြမယ်
03 Language — ဘာသာစကားရွေးချယ်ရန်
04 Home — ပင်မစာမျက်နှာ
05 Search — ရှာဖွေရန်
06 Categories — အမျိုးအစားများ
07 Site Details — အမွေအနှစ်နေရာ အသေးစိတ်
08 3D Viewer — သုံးဖက်မြင် ကြည့်ရှုရန်
09 AR Camera — AR ကင်မရာ
10 Bagan Map — ပုဂံမြေပုံ
11 Suggested Routes — အကြံပြုခရီးစဉ်များ
12 Favorites — အကြိုက်ဆုံးများ
13 Museum Information — ပြတိုက်အကြောင်း
14 Artifacts — ရှေးဟောင်းပစ္စည်းများ
15 History Timeline — သမိုင်းအချိန်ဇယား
16 Settings — ဆက်တင်များ
17 Profile — ကိုယ်ရေးအချက်အလက်
18 Help & Support — အကူအညီနှင့် ပံ့ပိုးမှု
19 About — အက်ပ်အကြောင်း
20 Exit — ထွက်ရန်

## Required QA
- Use editable native Compose text, not words baked into poster illustrations.
- Use consistent Bagan Heritage Museum capitalization and V3.5.3 version.
- No English misspellings on buttons; Myanmar text stored as Unicode UTF-8.
- Respect system status/navigation bars and display cutouts on Samsung One UI.
- 48 dp minimum touch targets, scalable fonts, scrolling on short displays.
- Keep ARCore checks and an explicit unsupported-device fallback; never crash.
- Do not promise 3D model rendering when GLB download or loading fails.
- Verify each screenshot with actual Android emulator and physical Samsung device; not yet done.
- The existing Kotlin routes differ from the exact 20 design storyboard labels and still require screen-by-screen remapping.

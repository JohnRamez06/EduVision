import os
import requests
import time

# ===================================================================
# ALL 120+ STUDENTS WITH GOOGLE DRIVE FILE IDs
# ===================================================================
STUDENTS = [
    {"id":"231006367","name":"محمد علاء لطفى","fileId":"1_niharFKW2_nScrP1uE-Rzz_c_ees93v"},
    {"id":"231015291","name":"بيشوى مرقس حبيب","fileId":"172CrGR6FqwjD4NeLm5NNbjVMChtYDa1L"},
    {"id":"231014184","name":"مرام تامر عبدالحى","fileId":"1THJ5C5L6PIKRB7eGjBxHlxWn1167KFBH"},
    {"id":"231014670","name":"رضوى شريف حماد","fileId":"1We2kGOKYeBLio8UAPhwSuJPhpQq2lFKK"},
    {"id":"231006507","name":"ندى شريف ابراهيم","fileId":"1Yhd5I0znA7zsOgjVVLdPWB8uIIABFInD"},
    {"id":"231005837","name":"مريم وائل البورصلى","fileId":"174uoXH146c0V__VmuTJMO5dRbe5UfANj"},
    {"id":"231006798","name":"حسين هشام فريد","fileId":"19or9fAszo6z1ySC1jH1_WHFIhujbYhuk"},
    {"id":"231004345","name":"فرح ياسر ابراهيم","fileId":"16Lmu_aYKHbwMwkAFoS--JeG5gD0-Hgj0"},
    {"id":"231014067","name":"زينه محمد ابراهيم","fileId":"1Z8pteqpVBeWU8eGV3WJ3Ck-3S2Mds4AF"},
    {"id":"231005936","name":"مريم محمد سالم","fileId":"1Cs1clYYgA-wXqbktlsQmMjDfXavYpE_n"},
    {"id":"231004779","name":"ماريو رافت عياد","fileId":"1CrBIxjj2OQJik1mRw2Ph0-5IUfN4UDxh"},
    {"id":"231014972","name":"براء ايمن عبدالعظيم","fileId":"1TiBOoYQQXD_utE4k3QlFazWJmQY1U54N"},
    {"id":"231006982","name":"ندى محمد ابراهيم","fileId":"1TM2-4godFdfauuoWe0ba6NZUXSOPILYh"},
    {"id":"231006760","name":"نور رضا ابوالخير","fileId":"1S5su5yVw5UGv2EJYgKJ4oiWoV7oS6tfG"},
    {"id":"231005898","name":"معاذ وائل سلام","fileId":"14z0CZPr2u6ncjsXzNcH2a89U4GjkuqGT"},
    {"id":"231005756","name":"شهد اسامه سعود","fileId":"1iwvEvXtXs4VIE5X_qRa74wExtzIYUn-9"},
    {"id":"231006916","name":"عبدالله خالد عمار","fileId":"1jrp6KFZn9H6oW7EaqbBxMpHE7q4DH27F"},
    {"id":"231006688","name":"بلال اشرف حسن","fileId":"1h5SKQuxWzzpy4kWZ6A2EZkHdGXxy17W6"},
    {"id":"231006359","name":"انس مصطفى مكاوى","fileId":"19MfSA-QcdBy9V9083ncTq1kTzRHC7C5O"},
    {"id":"231004095","name":"جون ماجد لبيب","fileId":"1g687tpuzFq9SnP_ar4zSHBWcxzOif9kz"},
    {"id":"231005820","name":"عمر خالد يوسف","fileId":"1jUDQMQI0PcTpCNkocFsNy8uZo1Kkn47N"},
    {"id":"231006309","name":"اروى يحيى سالمه","fileId":"1i_VBqtd_QVO97KdGje6vybvRN8RKtlF9"},
    {"id":"231006563","name":"محمود عمرو احمد","fileId":"1tKb-0QLlDg50GM3LsD8ku0pzsGkODau2"},
    {"id":"231002467","name":"شيرين احمد حسنين","fileId":"1hHhvLnqdeaoSfi-D4KSrC5slhIg1pXF0"},
    {"id":"231007895","name":"ناريمان عادل الازهرى","fileId":"1UenQOhilne4HoTmktKh32ZvKFSJYhk0u"},
    {"id":"231014770","name":"فريده احمد سليم","fileId":"1CIYUg9GdkWFdgODXBBc01Lv6Q6uky6OD"},
    {"id":"231015308","name":"عمر شريف الادهم","fileId":"1ktAKeBoetytE12mzUnYJKX6bDjdAamPZ"},
    {"id":"231004836","name":"لؤى وليد ابوالمعاطى","fileId":"1FqWJ80AmVL0N8fOB8jGBooJYy3wt2ePX"},
    {"id":"231005027","name":"ميرا عاطف صالح","fileId":"1qnWOzCoAQzYNEyIJs3qrn3KL-sNdXr7V"},
    {"id":"231004160","name":"عبدالله محمد شتات","fileId":"1yiBcdXA4_0NqgiMxVJcg73k2N1n96wJP"},
    {"id":"231014083","name":"هنا ايهاب على","fileId":"15gPRY59bGm-AwiKXuAPsCAnaX6uqXKZD"},
    {"id":"231004160","name":"عبدالله محمد شتات","fileId":"1DjcF91gUb6GiaSnwHb464dCS6BKCUqtX"},
    {"id":"231014373","name":"محمود احمد شلبى","fileId":"1wtKy8kWddf_ltHY8XRbuNmXdYRjWZF5h"},
    {"id":"231006822","name":"عبدالله عماد حسن","fileId":"18V66RsEXNopzZ1YD3UG8f3xMSI-eOolT"},
    {"id":"231006766","name":"زياد السيد حسن","fileId":"1ywGOQJi4h9VIC5GuWMEdDvZukzWQ5u8l"},
    {"id":"231014466","name":"روان طارق ابوالدهب","fileId":"1E5vWG6xIB2oyvUfwVQn1qLW1cVmKA00b"},
    {"id":"231006844","name":"ادهم هانى اسماعيل","fileId":"1xBHAJLbVaX4es5vMcnMiZAeyP-4QVMwu"},
    {"id":"231004206","name":"ريم حسين حسن","fileId":"1unWVDOr0wJTfkq3Mg7VoZHIjfY--UkgZ"},
    {"id":"231006901","name":"زياد خالد احمد","fileId":"1FkfGeaQQiSi9Bg2R2VeS-QYxERmPbZaw"},
    {"id":"231006804","name":"مارك هانى ابادير","fileId":"1gnfJrEcXWSEJWOYr-p3zBV7_uWP9QqoO"},
    {"id":"241004978","name":"عمر علاء الصناديدى","fileId":"1-S2ShiZvZgnE2kci6bcm5ANWZWjAznVL"},
    {"id":"231014763","name":"يوستينا ممدوح مينا","fileId":"1pULdhkTk-4ulCVlGjl4f9Y3AgZ546xtM"},
    {"id":"231005601","name":"امال يوسف صالح","fileId":"17xJqO0_bbqwtuffNx8rbsUEXCzq9FGjA"},
    {"id":"231005601","name":"امال يوسف صالح","fileId":"18BX9kI64rX9OJdkm74Xv1TtktarqnXcT"},
    {"id":"232004221","name":"اسماء عادل بيومى","fileId":"1kvcZGEfgl0PCJC0WcHZ4yCnMieHdZh5z"},
    {"id":"231006154","name":"ليندا احمد مصيلحى","fileId":"1l8NSWaNIOcv-eWrcbKPMxMGT0kO-yXdH"},
    {"id":"231008132","name":"مروان عبدالمنعم عبدالمنعم","fileId":"1aBkw0-Sf3IIgcNqGbobtvV6NVQRBLvzW"},
    {"id":"231004918","name":"محمد اسلام على","fileId":"132572vZqeac-rtmLi_9zRM2WyeB8r6Tu"},
    {"id":"231005865","name":"ضحى ايمن حسن","fileId":"1vhf6R2HHjT0CTC-bTUq8qgYfKtW5f0RN"},
    {"id":"231015004","name":"محمد احمد التهامى","fileId":"1ES0x7lU6v5s0aH_cAvphwR_M--TkoUWP"},
    {"id":"231014462","name":"تسنيم احمد الوزير","fileId":"1IfFJHHqRsbmN7p9AJqzGzVH2XDuo23D0"},
    {"id":"231014761","name":"همسه اشرف السخي","fileId":"1uTLd4ZFCodClny4Puv3AdEvC9-N6W4yN"},
    {"id":"231006502","name":"بسمله محمد محمد","fileId":"1XfK82h5IeVGtIIYv3HSI6thWsfwji2VS"},
    {"id":"231006272","name":"احمد خورشيد ميهوب","fileId":"1RlqC498EPnvwF3RJt8EgQ1DlULaVoKdo"},
    {"id":"231004567","name":"ياسين شريف الجوهري","fileId":"1boX7QGfoXcRHt0v2xt7QHLcSgYQk09ev"},
    {"id":"231004567","name":"ياسين شريف الجوهري","fileId":"1C6OIAxzdxVai861lKaAB_8e3ryXdclf9"},
    {"id":"231005711","name":"باسل اسامه سليمان","fileId":"19jAfV8ZjwsKLGZBBL_FDhtLAJkJlS8hB"},
    {"id":"211014850","name":"مروان محمد خلف","fileId":"1UyqDWtCMKyGRu5K3GP74bVD2zqavVXOl"},
    {"id":"231006900","name":"للوار صادق حسين","fileId":"1BBjubX9QacnSD2tg8WeZh_JYmMGgm18f"},
    {"id":"231014783","name":"منه الله عطيه","fileId":"1q7DGmpfxD-DZX67adQ7hH5B0jVi27dW9"},
    {"id":"231005915","name":"احمد فوزى الياسرجى","fileId":"1FM5hyT-5nbQjVWHuBL35zt7UK0z_09BA"},
    {"id":"231014666","name":"نور احمد محمد","fileId":"1KC1Xgnut0m04fHW4QB95yNF-_ZFm3_vn"},
    {"id":"231006613","name":"جنى محمد رياض","fileId":"18efGeWrYdJ61U4FjGACYjWywb-mYi82Y"},
    {"id":"231017969","name":"انجى على طه","fileId":"1-2tyU4xLIt676bIWAGb7BOPToKY9kESd"},
    {"id":"231006601","name":"نورالهدى اشرف محمود","fileId":"11PK08htS34vXmbGmBaNyh51UKxVAb7cy"},
    {"id":"231006131","name":"عمر حسام جاد","fileId":"1t9nEVT0EuejALfxZ-UOSqzUvNYHCCSnM"},
    {"id":"231015037","name":"اسراء عمرو سلامه","fileId":"1GyEqA9umpnGlx25_kujUQ4WUbOt5Iqz2"},
    {"id":"231014860","name":"رنا ياسر عفيفي","fileId":"1RnXvXlvVx2hZNQAKdIx8lSoSjfSb4xyq"},
    {"id":"231008132","name":"مروان عبدالمنعم عبدالمنعم","fileId":"1J7josqGfYieW6T2Vd5fFWUukBn_ivuAQ"},
    {"id":"231004649","name":"على سيد حسانين","fileId":"13E9nxwWWhwNLt9pv16wYL6xpDfgRkX8u"},
    {"id":"231015004","name":"محمد احمد التهامى","fileId":"1ol0LHOhDJaaSlt0SaiZ7cCzZVdxTGi57"},
    {"id":"231004431","name":"ياسين وفيق طولان","fileId":"1ZQzppXgq3ndp12Ucuxz_2BshX-ExeOPP"},
    {"id":"231014259","name":"مهاب امين حجازى","fileId":"1JeGC5YetKo1NLqqiajkVnENhj4TQS7EK"},
    {"id":"231014599","name":"يوسف احمد الصواف","fileId":"1k6jZXYrmOj2lambJfZFMy9hkV7F9cUCJ"},
    {"id":"231006928","name":"كريم محمد فتيح","fileId":"1cguXwO__QRVZuSRYIMboljxi8UeXmQ85"},
    {"id":"231006417","name":"مروان محمد الشامى","fileId":"11X-gLO4a7Jd_S155VU8AHQdEOqvN9utK"},
    {"id":"231014691","name":"زياد ايهاب احمد","fileId":"1j2loeiDcZtjdTPMBv4yUMyRttfmHO2qT"},
    {"id":"231014324","name":"نديم كامل كامل","fileId":"1K-4VcgfKWke-EZIEzN0tiPFn-9KIAxRD"},
    {"id":"231006879","name":"ياسين تامر عبدالحميد","fileId":"1xLPC_8hgx0_byHL44_c4YXcsL5vgFI7M"},
    {"id":"231005689","name":"ريتاج على على","fileId":"1PhMRgPjkz2IyEdQwMWKc3cmka2iR0p5n"},
    {"id":"231005430","name":"مهند وليد نصار","fileId":"1jExHIJEcCiVLJ9Y4DfvA5yYWO8pXqr7r"},
    {"id":"231004387","name":"يوسف حسين نور","fileId":"1OsvNlTiqZr6a0VqCBudYu32G39zkfBCq"},
    {"id":"231004747","name":"جيداء مجدى الخشاب","fileId":"1mRm7Dj3Ozty86PNWtC4Ya1ua_vRwemPG"},
    {"id":"231006572","name":"زياد محمد همام","fileId":"1JoS2Pd4H2XOE4ZRBhh6Hx_PGVJtJMfw-"},
    {"id":"231004727","name":"مروان وليد حجاج","fileId":"1DZrD8guFreVS4t-am_hsf59pMx7Yq8by"},
    {"id":"231005789","name":"روان عاطف حسن","fileId":"19Fl3g6XhtCy8T7EPK4DQDJrcNKIk-m73"},
    {"id":"231014241","name":"مصطفى وليد الخولي","fileId":"1CIgH622qqnFO83Wl1hHiizwAJlAIdw3r"},
    {"id":"231004224","name":"عمر نشأت على","fileId":"1QxqJY2GmxR2nep_u3qEGrXRPDVXuAqCc"},
    {"id":"231014002","name":"سما سيد سليمان","fileId":"1GCVdj2ukCVt2Q_F2DrZZSRVj7MT2pDve"},
    {"id":"231014849","name":"ديفيد عاطف مكاريوس","fileId":"1sixqXXeDy6S8EGMq1_DGEf8215kPYSjA"},
    {"id":"231014849","name":"ديفيد عاطف مكاريوس","fileId":"1PDKutq7xR0mFp4Q69g3VRdygj20h_ejD"},
    {"id":"231014025","name":"ندى عبدالعزيز امنه","fileId":"1reEUrIekhARfDORmjpcAc0VMpJ90UfJI"},
    {"id":"231014449","name":"يوسف كريم محمد","fileId":"19V8Wq9l5Y758aft7La664YM1TDZ2G45t"},
    {"id":"231014457","name":"زياد محمد مخلوف","fileId":"1C0jaBCQhYE5WtbPOTlIrk1Q5ZN5rk0Iv"},
    {"id":"231006127","name":"ميرنا عبدالعظيم مصطفى","fileId":"1eE28TodJeI0FQAZHdEJSOtcOmumILyqs"},
    {"id":"231004285","name":"عبدالله اشرف عبدالعزيز","fileId":"19hZ73pHzEumJIabMm5b0XFBTEJN_lbPG"},
    {"id":"231005940","name":"مصطفى محمد جبر","fileId":"16ObATtzBbDORnmcjk_SPjaMrb5yLCx3u"},
    {"id":"231014744","name":"محمد احمد مرسي","fileId":"13EFTNkfTA4vEFshZD2TgHvcazgKD8yPb"},
    {"id":"231006574","name":"ليال احمد موسى","fileId":"1932IM9EgQ4BUZe4nMdyXu8K_2S40nfEv"},
    {"id":"231006950","name":"يوسف محمد نحله","fileId":"1c4sgOVARFz3OJWJLXZDkm4kWkQjHevKv"},
    {"id":"231014539","name":"ياسين السيد الهادى","fileId":"1cbfvYFbeh3gWKbdI5czxpcWiNBjPICd2"},
    {"id":"231005333","name":"ضحى محمود ضيف","fileId":"1ZdLWeqmYjE65_lL0BebKTdMtUoWfdASe"},
    {"id":"231005400","name":"شهد محمد جبريل","fileId":"1pmZLPTUlCrP8p2W6xyI343E2JG52WYJg"},
    {"id":"231014166","name":"نور احمد الجندى","fileId":"1LAHxo_RwZj3TZmlv_jaolPBwflzUMLS6"},
    {"id":"231014449","name":"يوسف كريم محمد","fileId":"1ts5KoZk4SuKL6oSGL3bBSiB-ef06MPdP"},
    {"id":"231006335","name":"محمد ياسر فرج","fileId":"1quNROw-4csjvyvPRsa_Rnj4Apqi7KCSM"},
    {"id":"231006825","name":"انس محمد ستيت","fileId":"1UcgVefaii-tcME5AA6IAf0qIJAAf28va"},
    {"id":"231014647","name":"عبدالرحمن سيد توفيق","fileId":"1a_0YxRbDQ4Cnk_fgXRC_Z9h14tc09qnW"},
    {"id":"231014333","name":"عبدالرحمن طارق نور","fileId":"18Ucq-myMFpw48ieZkWVvy8otZqAwCEw9"},
    {"id":"231004419","name":"نور خالد خليف","fileId":"1ePZayqbgEYt56dyEerd-sbttN6ak2hyU"},
    {"id":"231015069","name":"ساره رائف عبدالسلام","fileId":"1lzy50Z5FG8SAZz8FfXGoiNGBbCxGOUdy"},
    {"id":"231006012","name":"حازم اسامه حجاج","fileId":"1u7AgwKZpDr_pPVKRQlzrWmt2au-LiraR"},
    {"id":"231014590","name":"يوسف عبدالمنعم سالمان","fileId":"1volYaMLDiZ511pPmSpdzuqfx6xbY6grk"},
    {"id":"231006511","name":"زيد محمد حامد","fileId":"1bllpxDsnrSGG0LBprzNtkLnI0R_Tidlw"},
    {"id":"231006695","name":"عمر عماد الحبشى","fileId":"1qrWZA_HCqFBgXZTif5L1dSka4_MG8iUC"},
    {"id":"231014333","name":"عبدالرحمن طارق نور","fileId":"1org0ltKpaVPl6v0Au-PYQvKheOzST2zT"},
    {"id":"231016666","name":"ملك احمد ابراهيم","fileId":"1SUfi5njCSvDTUuydZTwofvja6mR49uo4"},
    {"id":"231006856","name":"يوسف محمد ابراهيم","fileId":"1FGiGJQHGGyUEvciEYe27rjqyq2V4GsBn"},
    {"id":"231014342","name":"فاطمه خليل خليل","fileId":"1Ml1qjCdm2GZ8v_pCuJbdPt2puiWUUfH0"},
    {"id":"231005501","name":"نانسى محمد عرفات","fileId":"1d7vwkmxYKmEhHlmPvpV-qKgTG9xMcvA4"},
    {"id":"231015218","name":"عبدالرحمن محمد عبدالنبى","fileId":"1DpW0I6_wOfGU4fd7dz__PX_RUot_go8i"},
    {"id":"231004713","name":"يحيى احمد الحاوى","fileId":"1lFmcC68a48dtCOkvbHnOs0ly6QhpXbJP"},
    {"id":"231014786","name":"احمد محمد محمد","fileId":"1SoqrtK-440MJbsGSWppYa7haAyYjHh50"},
    {"id":"231005073","name":"عبدالرحمن محمد الصاوى","fileId":"1KszOqwWU1GH7keQVA4ikyuVriDbxXt4R"},
    {"id":"231014755","name":"عبدالرحمن احمد عليوه","fileId":"1Gs19IhlnDYTJEMuVyJFgd7KumyDHy4MP"},
    {"id":"231006586","name":"رقيه حمدى ربه","fileId":"17o3s6GiZsBgSBHlak8GmrhhEzy6bte48"},
    {"id":"231014395","name":"احمد فاروق دنيا","fileId":"1xkJCEt8K4l_OnmNmYWNvQ304xI_Ik1Vd"},
]

# ===================================================================
# DOWNLOAD SCRIPT - DO NOT EDIT BELOW THIS LINE
# ===================================================================

BASE_DIR = "face_enrollment"
os.makedirs(BASE_DIR, exist_ok=True)

total = len(STUDENTS)
success = 0
failed = 0

print("=" * 60)
print(f"EDUVISION - DOWNLOADING {total} STUDENT PHOTOS")
print("=" * 60)

for i, student in enumerate(STUDENTS):
    folder = os.path.join(BASE_DIR, student["id"])
    os.makedirs(folder, exist_ok=True)
    filepath = os.path.join(folder, "photo_1.jpg")
    
    # Skip if already downloaded
    if os.path.exists(filepath) and os.path.getsize(filepath) > 10000:
        print(f"[{i+1}/{total}] ⏭️  {student['id']} - {student['name']} (already downloaded)")
        success += 1
        continue
    
    url = f"https://drive.google.com/uc?export=download&id={student['fileId']}"
    
    try:
        print(f"[{i+1}/{total}] ⬇️  {student['id']} - {student['name']}...", end=" ")
        response = requests.get(url, timeout=30, allow_redirects=True)
        
        if response.status_code == 200 and len(response.content) > 10000:
            with open(filepath, "wb") as f:
                f.write(response.content)
            file_size = len(response.content) // 1024
            print(f"✅ ({file_size} KB)")
            success += 1
        elif len(response.content) <= 10000:
            print(f"❌ File too small ({len(response.content)} bytes) - may need login")
            failed += 1
        else:
            print(f"❌ HTTP {response.status_code}")
            failed += 1
    except Exception as e:
        print(f"❌ Error: {str(e)[:50]}")
        failed += 1
    
    # Small delay between downloads
    time.sleep(0.3)

print("=" * 60)
print(f"RESULTS: {success} success | {failed} failed | {total} total")
print(f"Photos saved to: {os.path.abspath(BASE_DIR)}/")
print("=" * 60)
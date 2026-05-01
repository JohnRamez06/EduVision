import { useState, useCallback, useRef } from "react";

const STUDENTS = [{"id":"231006367","name":"محمد علاء لطفى","fileId":"1_niharFKW2_nScrP1uE-Rzz_c_ees93v"},{"id":"231015291","name":"بيشوى مرقس حبيب","fileId":"172CrGR6FqwjD4NeLm5NNbjVMChtYDa1L"},{"id":"231014184","name":"مرام تامر عبدالحى","fileId":"1THJ5C5L6PIKRB7eGjBxHlxWn1167KFBH"},{"id":"231014670","name":"رضوى شريف حماد","fileId":"1We2kGOKYeBLio8UAPhwSuJPhpQq2lFKK"},{"id":"231006507","name":"ندى شريف ابراهيم","fileId":"1Yhd5I0znA7zsOgjVVLdPWB8uIIABFInD"},{"id":"231005837","name":"مريم وائل البورصلى","fileId":"174uoXH146c0V__VmuTJMO5dRbe5UfANj"},{"id":"231006798","name":"حسين هشام فريد","fileId":"19or9fAszo6z1ySC1jH1_WHFIhujbYhuk"},{"id":"231004345","name":"فرح ياسر ابراهيم","fileId":"16Lmu_aYKHbwMwkAFoS--JeG5gD0-Hgj0"},{"id":"231014067","name":"زينه محمد ابراهيم","fileId":"1Z8pteqpVBeWU8eGV3WJ3Ck-3S2Mds4AF"},{"id":"231005936","name":"مريم محمد سالم","fileId":"1Cs1clYYgA-wXqbktlsQmMjDfXavYpE_n"},{"id":"231004779","name":"ماريو رافت عياد","fileId":"1CrBIxjj2OQJik1mRw2Ph0-5IUfN4UDxh"},{"id":"231014972","name":"براء ايمن عبدالعظيم","fileId":"1TiBOoYQQXD_utE4k3QlFazWJmQY1U54N"},{"id":"231006982","name":"ندى محمد ابراهيم","fileId":"1TM2-4godFdfauuoWe0ba6NZUXSOPILYh"},{"id":"231006760","name":"نور رضا ابوالخير","fileId":"1S5su5yVw5UGv2EJYgKJ4oiWoV7oS6tfG"},{"id":"231005898","name":"معاذ وائل سلام","fileId":"14z0CZPr2u6ncjsXzNcH2a89U4GjkuqGT"},{"id":"231005756","name":"شهد اسامه سعود","fileId":"1iwvEvXtXs4VIE5X_qRa74wExtzIYUn-9"},{"id":"231006916","name":"عبدالله خالد عمار","fileId":"1jrp6KFZn9H6oW7EaqbBxMpHE7q4DH27F"},{"id":"231006688","name":"بلال اشرف حسن","fileId":"1h5SKQuxWzzpy4kWZ6A2EZkHdGXxy17W6"},{"id":"231006359","name":"انس مصطفى مكاوى","fileId":"19MfSA-QcdBy9V9083ncTq1kTzRHC7C5O"},{"id":"231004095","name":"جون ماجد لبيب","fileId":"1g687tpuzFq9SnP_ar4zSHBWcxzOif9kz"},{"id":"231005820","name":"عمر خالد يوسف","fileId":"1jUDQMQI0PcTpCNkocFsNy8uZo1Kkn47N"},{"id":"231006309","name":"اروى يحيى سالمه","fileId":"1i_VBqtd_QVO97KdGje6vybvRN8RKtlF9"},{"id":"231006563","name":"محمود عمرو احمد","fileId":"1tKb-0QLlDg50GM3LsD8ku0pzsGkODau2"},{"id":"231002467","name":"شيرين احمد حسنين","fileId":"1hHhvLnqdeaoSfi-D4KSrC5slhIg1pXF0"},{"id":"231007895","name":"ناريمان عادل الازهرى","fileId":"1UenQOhilne4HoTmktKh32ZvKFSJYhk0u"},{"id":"231014770","name":"فريده احمد سليم","fileId":"1CIYUg9GdkWFdgODXBBc01Lv6Q6uky6OD"},{"id":"231015308","name":"عمر شريف الادهم","fileId":"1ktAKeBoetytE12mzUnYJKX6bDjdAamPZ"},{"id":"231004836","name":"لؤى وليد ابوالمعاطى","fileId":"1FqWJ80AmVL0N8fOB8jGBooJYy3wt2ePX"},{"id":"231005027","name":"ميرا عاطف صالح","fileId":"1qnWOzCoAQzYNEyIJs3qrn3KL-sNdXr7V"},{"id":"231004160","name":"عبدالله محمد شتات","fileId":"1yiBcdXA4_0NqgiMxVJcg73k2N1n96wJP"},{"id":"231014083","name":"هنا ايهاب على","fileId":"15gPRY59bGm-AwiKXuAPsCAnaX6uqXKZD"},{"id":"231004160","name":"عبدالله محمد شتات","fileId":"1DjcF91gUb6GiaSnwHb464dCS6BKCUqtX"},{"id":"231014373","name":"محمود احمد شلبى","fileId":"1wtKy8kWddf_ltHY8XRbuNmXdYRjWZF5h"},{"id":"231006822","name":"عبدالله عماد حسن","fileId":"18V66RsEXNopzZ1YD3UG8f3xMSI-eOolT"},{"id":"231006766","name":"زياد السيد حسن","fileId":"1ywGOQJi4h9VIC5GuWMEdDvZukzWQ5u8l"},{"id":"231014466","name":"روان طارق ابوالدهب","fileId":"1E5vWG6xIB2oyvUfwVQn1qLW1cVmKA00b"},{"id":"231006844","name":"ادهم هانى اسماعيل","fileId":"1xBHAJLbVaX4es5vMcnMiZAeyP-4QVMwu"},{"id":"231004206","name":"ريم حسين حسن","fileId":"1unWVDOr0wJTfkq3Mg7VoZHIjfY--UkgZ"},{"id":"231006901","name":"زياد خالد احمد","fileId":"1FkfGeaQQiSi9Bg2R2VeS-QYxERmPbZaw"},{"id":"231006804","name":"مارك هانى ابادير","fileId":"1gnfJrEcXWSEJWOYr-p3zBV7_uWP9QqoO"},{"id":"241004978","name":"عمر علاء الصناديدى","fileId":"1-S2ShiZvZgnE2kci6bcm5ANWZWjAznVL"},{"id":"231014763","name":"يوستينا ممدوح مينا","fileId":"1pULdhkTk-4ulCVlGjl4f9Y3AgZ546xtM"},{"id":"231005601","name":"امال يوسف صالح","fileId":"17xJqO0_bbqwtuffNx8rbsUEXCzq9FGjA"},{"id":"231005601","name":"امال يوسف صالح","fileId":"18BX9kI64rX9OJdkm74Xv1TtktarqnXcT"},{"id":"232004221","name":"اسماء عادل بيومى","fileId":"1kvcZGEfgl0PCJC0WcHZ4yCnMieHdZh5z"},{"id":"231006154","name":"ليندا احمد مصيلحى","fileId":"1l8NSWaNIOcv-eWrcbKPMxMGT0kO-yXdH"},{"id":"231008132","name":"مروان عبدالمنعم عبدالمنعم","fileId":"1aBkw0-Sf3IIgcNqGbobtvV6NVQRBLvzW"},{"id":"231004918","name":"محمد اسلام على","fileId":"132572vZqeac-rtmLi_9zRM2WyeB8r6Tu"},{"id":"231005865","name":"ضحى ايمن حسن","fileId":"1vhf6R2HHjT0CTC-bTUq8qgYfKtW5f0RN"},{"id":"231015004","name":"محمد احمد التهامى","fileId":"1ES0x7lU6v5s0aH_cAvphwR_M--TkoUWP"},{"id":"231014462","name":"تسنيم احمد الوزير","fileId":"1IfFJHHqRsbmN7p9AJqzGzVH2XDuo23D0"},{"id":"231014761","name":"همسه اشرف السخي","fileId":"1uTLd4ZFCodClny4Puv3AdEvC9-N6W4yN"},{"id":"231006502","name":"بسمله محمد محمد","fileId":"1XfK82h5IeVGtIIYv3HSI6thWsfwji2VS"},{"id":"231006272","name":"احمد خورشيد ميهوب","fileId":"1RlqC498EPnvwF3RJt8EgQ1DlULaVoKdo"},{"id":"231004567","name":"ياسين شريف الجوهري","fileId":"1boX7QGfoXcRHt0v2xt7QHLcSgYQk09ev"},{"id":"231004567","name":"ياسين شريف الجوهري","fileId":"1C6OIAxzdxVai861lKaAB_8e3ryXdclf9"},{"id":"231005711","name":"باسل اسامه سليمان","fileId":"19jAfV8ZjwsKLGZBBL_FDhtLAJkJlS8hB"},{"id":"211014850","name":"مروان محمد خلف","fileId":"1UyqDWtCMKyGRu5K3GP74bVD2zqavVXOl"},{"id":"231006900","name":"للوار صادق حسين","fileId":"1BBjubX9QacnSD2tg8WeZh_JYmMGgm18f"},{"id":"231014783","name":"منه الله عطيه","fileId":"1q7DGmpfxD-DZX67adQ7hH5B0jVi27dW9"},{"id":"231005915","name":"احمد فوزى الياسرجى","fileId":"1FM5hyT-5nbQjVWHuBL35zt7UK0z_09BA"},{"id":"231014666","name":"نور احمد محمد","fileId":"1KC1Xgnut0m04fHW4QB95yNF-_ZFm3_vn"},{"id":"231006613","name":"جنى محمد رياض","fileId":"18efGeWrYdJ61U4FjGACYjWywb-mYi82Y"},{"id":"231017969","name":"انجى على طه","fileId":"1-2tyU4xLIt676bIWAGb7BOPToKY9kESd"},{"id":"231006601","name":"نورالهدى اشرف محمود","fileId":"11PK08htS34vXmbGmBaNyh51UKxVAb7cy"},{"id":"231006131","name":"عمر حسام جاد","fileId":"1t9nEVT0EuejALfxZ-UOSqzUvNYHCCSnM"},{"id":"231015037","name":"اسراء عمرو سلامه","fileId":"1GyEqA9umpnGlx25_kujUQ4WUbOt5Iqz2"},{"id":"231014860","name":"رنا ياسر عفيفي","fileId":"1RnXvXlvVx2hZNQAKdIx8lSoSjfSb4xyq"},{"id":"231008132","name":"مروان عبدالمنعم عبدالمنعم","fileId":"1J7josqGfYieW6T2Vd5fFWUukBn_ivuAQ"},{"id":"231004649","name":"على سيد حسانين","fileId":"13E9nxwWWhwNLt9pv16wYL6xpDfgRkX8u"},{"id":"231015004","name":"محمد احمد التهامى","fileId":"1ol0LHOhDJaaSlt0SaiZ7cCzZVdxTGi57"},{"id":"231004431","name":"ياسين وفيق طولان","fileId":"1ZQzppXgq3ndp12Ucuxz_2BshX-ExeOPP"},{"id":"231014259","name":"مهاب امين حجازى","fileId":"1JeGC5YetKo1NLqqiajkVnENhj4TQS7EK"},{"id":"231014599","name":"يوسف احمد الصواف","fileId":"1k6jZXYrmOj2lambJfZFMy9hkV7F9cUCJ"},{"id":"231006928","name":"كريم محمد فتيح","fileId":"1cguXwO__QRVZuSRYIMboljxi8UeXmQ85"},{"id":"231006417","name":"مروان محمد الشامى","fileId":"11X-gLO4a7Jd_S155VU8AHQdEOqvN9utK"},{"id":"231014691","name":"زياد ايهاب احمد","fileId":"1j2loeiDcZtjdTPMBv4yUMyRttfmHO2qT"},{"id":"231014324","name":"نديم كامل كامل","fileId":"1K-4VcgfKWke-EZIEzN0tiPFn-9KIAxRD"},{"id":"231006879","name":"ياسين تامر عبدالحميد","fileId":"1xLPC_8hgx0_byHL44_c4YXcsL5vgFI7M"},{"id":"231005689","name":"ريتاج على على","fileId":"1PhMRgPjkz2IyEdQwMWKc3cmka2iR0p5n"},{"id":"231005430","name":"مهند وليد نصار","fileId":"1jExHIJEcCiVLJ9Y4DfvA5yYWO8pXqr7r"},{"id":"231004387","name":"يوسف حسين نور","fileId":"1OsvNlTiqZr6a0VqCBudYu32G39zkfBCq"},{"id":"231004747","name":"جيداء مجدى الخشاب","fileId":"1mRm7Dj3Ozty86PNWtC4Ya1ua_vRwemPG"},{"id":"231006572","name":"زياد محمد همام","fileId":"1JoS2Pd4H2XOE4ZRBhh6Hx_PGVJtJMfw-"},{"id":"231004727","name":"مروان وليد حجاج","fileId":"1DZrD8guFreVS4t-am_hsf59pMx7Yq8by"},{"id":"231005789","name":"روان عاطف حسن","fileId":"19Fl3g6XhtCy8T7EPK4DQDJrcNKIk-m73"},{"id":"231014241","name":"مصطفى وليد الخولي","fileId":"1CIgH622qqnFO83Wl1hHiizwAJlAIdw3r"},{"id":"231004224","name":"عمر نشأت على","fileId":"1QxqJY2GmxR2nep_u3qEGrXRPDVXuAqCc"},{"id":"231014002","name":"سما سيد سليمان","fileId":"1GCVdj2ukCVt2Q_F2DrZZSRVj7MT2pDve"},{"id":"231014849","name":"ديفيد عاطف مكاريوس","fileId":"1sixqXXeDy6S8EGMq1_DGEf8215kPYSjA"},{"id":"231014849","name":"ديفيد عاطف مكاريوس","fileId":"1PDKutq7xR0mFp4Q69g3VRdygj20h_ejD"},{"id":"231014025","name":"ندى عبدالعزيز امنه","fileId":"1reEUrIekhARfDORmjpcAc0VMpJ90UfJI"},{"id":"231014449","name":"يوسف كريم محمد","fileId":"19V8Wq9l5Y758aft7La664YM1TDZ2G45t"},{"id":"231014457","name":"زياد محمد مخلوف","fileId":"1C0jaBCQhYE5WtbPOTlIrk1Q5ZN5rk0Iv"},{"id":"231006127","name":"ميرنا عبدالعظيم مصطفى","fileId":"1eE28TodJeI0FQAZHdEJSOtcOmumILyqs"},{"id":"231004285","name":"عبدالله اشرف عبدالعزيز","fileId":"19hZ73pHzEumJIabMm5b0XFBTEJN_lbPG"},{"id":"231005940","name":"مصطفى محمد جبر","fileId":"16ObATtzBbDORnmcjk_SPjaMrb5yLCx3u"},{"id":"231014744","name":"محمد احمد مرسي","fileId":"13EFTNkfTA4vEFshZD2TgHvcazgKD8yPb"},{"id":"231006574","name":"ليال احمد موسى","fileId":"1932IM9EgQ4BUZe4nMdyXu8K_2S40nfEv"},{"id":"231006950","name":"يوسف محمد نحله","fileId":"1c4sgOVARFz3OJWJLXZDkm4kWkQjHevKv"},{"id":"231014539","name":"ياسين السيد الهادى","fileId":"1cbfvYFbeh3gWKbdI5czxpcWiNBjPICd2"},{"id":"231005333","name":"ضحى محمود ضيف","fileId":"1ZdLWeqmYjE65_lL0BebKTdMtUoWfdASe"},{"id":"231005400","name":"شهد محمد جبريل","fileId":"1pmZLPTUlCrP8p2W6xyI343E2JG52WYJg"},{"id":"231014166","name":"نور احمد الجندى","fileId":"1LAHxo_RwZj3TZmlv_jaolPBwflzUMLS6"},{"id":"231014449","name":"يوسف كريم محمد","fileId":"1ts5KoZk4SuKL6oSGL3bBSiB-ef06MPdP"},{"id":"231006335","name":"محمد ياسر فرج","fileId":"1quNROw-4csjvyvPRsa_Rnj4Apqi7KCSM"},{"id":"231006825","name":"انس محمد ستيت","fileId":"1UcgVefaii-tcME5AA6IAf0qIJAAf28va"},{"id":"231014647","name":"عبدالرحمن سيد توفيق","fileId":"1a_0YxRbDQ4Cnk_fgXRC_Z9h14tc09qnW"},{"id":"231014333","name":"عبدالرحمن طارق نور","fileId":"18Ucq-myMFpw48ieZkWVvy8otZqAwCEw9"},{"id":"231004419","name":"نور خالد خليف","fileId":"1ePZayqbgEYt56dyEerd-sbttN6ak2hyU"},{"id":"231015069","name":"ساره رائف عبدالسلام","fileId":"1lzy50Z5FG8SAZz8FfXGoiNGBbCxGOUdy"},{"id":"231006012","name":"حازم اسامه حجاج","fileId":"1u7AgwKZpDr_pPVKRQlzrWmt2au-LiraR"},{"id":"231014590","name":"يوسف عبدالمنعم سالمان","fileId":"1volYaMLDiZ511pPmSpdzuqfx6xbY6grk"},{"id":"231006511","name":"زيد محمد حامد","fileId":"1bllpxDsnrSGG0LBprzNtkLnI0R_Tidlw"},{"id":"231006695","name":"عمر عماد الحبشى","fileId":"1qrWZA_HCqFBgXZTif5L1dSka4_MG8iUC"},{"id":"231014333","name":"عبدالرحمن طارق نور","fileId":"1org0ltKpaVPl6v0Au-PYQvKheOzST2zT"},{"id":"231016666","name":"ملك احمد ابراهيم","fileId":"1SUfi5njCSvDTUuydZTwofvja6mR49uo4"},{"id":"231006856","name":"يوسف محمد ابراهيم","fileId":"1FGiGJQHGGyUEvciEYe27rjqyq2V4GsBn"},{"id":"231014342","name":"فاطمه خليل خليل","fileId":"1Ml1qjCdm2GZ8v_pCuJbdPt2puiWUUfH0"},{"id":"231005501","name":"نانسى محمد عرفات","fileId":"1d7vwkmxYKmEhHlmPvpV-qKgTG9xMcvA4"},{"id":"231015218","name":"عبدالرحمن محمد عبدالنبى","fileId":"1DpW0I6_wOfGU4fd7dz__PX_RUot_go8i"},{"id":"231004713","name":"يحيى احمد الحاوى","fileId":"1lFmcC68a48dtCOkvbHnOs0ly6QhpXbJP"},{"id":"231014786","name":"احمد محمد محمد","fileId":"1SoqrtK-440MJbsGSWppYa7haAyYjHh50"},{"id":"231005073","name":"عبدالرحمن محمد الصاوى","fileId":"1KszOqwWU1GH7keQVA4ikyuVriDbxXt4R"},{"id":"231014755","name":"عبدالرحمن احمد عليوه","fileId":"1Gs19IhlnDYTJEMuVyJFgd7KumyDHy4MP"},{"id":"231006586","name":"رقيه حمدى ربه","fileId":"17o3s6GiZsBgSBHlak8GmrhhEzy6bte48"},{"id":"231014395","name":"احمد فاروق دنيا","fileId":"1xkJCEt8K4l_OnmNmYWNvQ304xI_Ik1Vd"}];

const STATUS = { idle: "idle", analyzing: "analyzing", done: "done", error: "error" };

const thumbUrl = (fileId) =>
  `https://drive.google.com/thumbnail?id=${fileId}&sz=w200`;

const uniqueStudents = Array.from(
  STUDENTS.reduce((m, s) => {
    if (!m.has(s.id)) m.set(s.id, { ...s, allFileIds: [s.fileId] });
    else m.get(s.id).allFileIds.push(s.fileId);
    return m;
  }, new Map()).values()
);

async function analyzePhotoWithClaude(fileId, studentName) {
  const imageUrl = thumbUrl(fileId);
  const resp = await fetch("https://api.anthropic.com/v1/messages", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      model: "claude-sonnet-4-20250514",
      max_tokens: 1000,
      messages: [{
        role: "user",
        content: [
          {
            type: "image",
            source: { type: "url", url: imageUrl }
          },
          {
            type: "text",
            text: `You are analyzing a student photo for a university face enrollment system. The student's name is "${studentName}".

Analyze this photo and respond ONLY with a valid JSON object (no markdown, no explanation):
{
  "faceDetected": true or false,
  "faceCount": number of faces detected,
  "quality": "good" | "acceptable" | "poor",
  "qualityIssues": [] or list of issues like ["blurry","low_light","face_partially_hidden","multiple_faces","no_face","sunglasses","mask"],
  "enrollmentReady": true if exactly 1 clear face with no major issues,
  "lightingScore": 1-10,
  "clarity": 1-10,
  "faceAngle": "frontal" | "slight_angle" | "profile" | "unknown",
  "notes": "one brief sentence about the photo"
}`
          }
        ]
      }]
    })
  });
  const data = await resp.json();
  const text = data.content?.map(b => b.text || "").join("") || "";
  const clean = text.replace(/```json|```/g, "").trim();
  return JSON.parse(clean);
}

export default function FaceEnrollmentTool() {
  const [results, setResults] = useState({});
  const [running, setRunning] = useState(false);
  const [progress, setProgress] = useState({ done: 0, total: 0 });
  const [selected, setSelected] = useState(null);
  const [filter, setFilter] = useState("all");
  const [search, setSearch] = useState("");
  const abortRef = useRef(false);

  const getStatus = (id) => results[id]?.status || STATUS.idle;

  const runAnalysis = useCallback(async () => {
    abortRef.current = false;
    setRunning(true);
    const toRun = uniqueStudents.filter(s => !results[s.id] || results[s.id].status === STATUS.error);
    setProgress({ done: 0, total: toRun.length });

    for (let i = 0; i < toRun.length; i++) {
      if (abortRef.current) break;
      const student = toRun[i];
      setResults(prev => ({ ...prev, [student.id]: { status: STATUS.analyzing } }));
      try {
        const analysis = await analyzePhotoWithClaude(student.fileId, student.name);
        setResults(prev => ({ ...prev, [student.id]: { status: STATUS.done, ...analysis } }));
      } catch (e) {
        setResults(prev => ({ ...prev, [student.id]: { status: STATUS.error, error: e.message } }));
      }
      setProgress(prev => ({ ...prev, done: prev.done + 1 }));
      await new Promise(r => setTimeout(r, 400));
    }
    setRunning(false);
  }, [results]);

  const stopAnalysis = () => { abortRef.current = true; };

  const runSingle = async (student) => {
    setResults(prev => ({ ...prev, [student.id]: { status: STATUS.analyzing } }));
    try {
      const analysis = await analyzePhotoWithClaude(student.fileId, student.name);
      setResults(prev => ({ ...prev, [student.id]: { status: STATUS.done, ...analysis } }));
    } catch (e) {
      setResults(prev => ({ ...prev, [student.id]: { status: STATUS.error, error: e.message } }));
    }
  };

  const stats = {
    total: uniqueStudents.length,
    analyzed: Object.values(results).filter(r => r.status === STATUS.done).length,
    ready: Object.values(results).filter(r => r.enrollmentReady === true).length,
    issues: Object.values(results).filter(r => r.status === STATUS.done && !r.enrollmentReady).length,
    errors: Object.values(results).filter(r => r.status === STATUS.error).length,
  };

  const filtered = uniqueStudents.filter(s => {
    const r = results[s.id];
    const matchFilter =
      filter === "all" ||
      (filter === "ready" && r?.enrollmentReady === true) ||
      (filter === "issues" && r?.status === STATUS.done && !r.enrollmentReady) ||
      (filter === "pending" && (!r || r.status === STATUS.idle)) ||
      (filter === "error" && r?.status === STATUS.error);
    const matchSearch = !search || s.name.includes(search) || s.id.includes(search);
    return matchFilter && matchSearch;
  });

  const exportSQL = () => {
    const readyStudents = uniqueStudents.filter(s => results[s.id]?.enrollmentReady);
    const lines = readyStudents.map(s =>
      `-- Student: ${s.name} (${s.id})\nUPDATE students SET consent_given = 1, consent_date = NOW() WHERE student_number = '${s.id}';`
    );
    const blob = new Blob([lines.join("\n\n")], { type: "text/sql" });
    const a = document.createElement("a");
    a.href = URL.createObjectURL(blob);
    a.download = "enrollment_ready_students.sql";
    a.click();
  };

  const qualityColor = (q) =>
    q === "good" ? "var(--color-text-success)" :
    q === "acceptable" ? "var(--color-text-warning)" : "var(--color-text-danger)";

  const qualityBg = (q) =>
    q === "good" ? "var(--color-background-success)" :
    q === "acceptable" ? "var(--color-background-warning)" : "var(--color-background-danger)";

  return (
    <div style={{ padding: "1.5rem 0" }}>
      <h2 style={{ fontSize: 18, fontWeight: 500, marginBottom: 4 }}>
        EduVision — face enrollment photo analyzer
      </h2>
      <p style={{ fontSize: 13, color: "var(--color-text-secondary)", marginBottom: "1.5rem" }}>
        {uniqueStudents.length} students · {STUDENTS.length} total photos (some students have multiple)
      </p>

      {/* Stats row */}
      <div style={{ display: "grid", gridTemplateColumns: "repeat(5, 1fr)", gap: 8, marginBottom: "1.5rem" }}>
        {[
          { label: "Total students", value: stats.total, color: "var(--color-text-primary)" },
          { label: "Analyzed", value: stats.analyzed, color: "var(--color-text-primary)" },
          { label: "Ready to enroll", value: stats.ready, color: "var(--color-text-success)" },
          { label: "Need review", value: stats.issues, color: "var(--color-text-warning)" },
          { label: "Errors", value: stats.errors, color: "var(--color-text-danger)" },
        ].map(({ label, value, color }) => (
          <div key={label} style={{ background: "var(--color-background-secondary)", borderRadius: "var(--border-radius-md)", padding: "0.75rem 1rem" }}>
            <div style={{ fontSize: 12, color: "var(--color-text-secondary)", marginBottom: 2 }}>{label}</div>
            <div style={{ fontSize: 22, fontWeight: 500, color }}>{value}</div>
          </div>
        ))}
      </div>

      {/* Progress bar */}
      {(running || progress.total > 0) && (
        <div style={{ marginBottom: "1.25rem" }}>
          <div style={{ display: "flex", justifyContent: "space-between", fontSize: 12, color: "var(--color-text-secondary)", marginBottom: 4 }}>
            <span>{running ? `Analyzing… ${progress.done} / ${progress.total}` : `Complete — ${progress.done} processed`}</span>
            <span>{Math.round((progress.done / Math.max(progress.total, 1)) * 100)}%</span>
          </div>
          <div style={{ height: 4, background: "var(--color-background-secondary)", borderRadius: 2, overflow: "hidden" }}>
            <div style={{
              height: "100%", borderRadius: 2,
              background: running ? "var(--color-text-info)" : "var(--color-text-success)",
              width: `${(progress.done / Math.max(progress.total, 1)) * 100}%`,
              transition: "width 0.3s ease"
            }} />
          </div>
        </div>
      )}

      {/* Controls */}
      <div style={{ display: "flex", gap: 8, marginBottom: "1.25rem", flexWrap: "wrap" }}>
        <button onClick={running ? stopAnalysis : runAnalysis} style={{ fontWeight: 500 }}>
          {running ? "Stop" : stats.analyzed === 0 ? "Analyze all photos" : "Continue analysis"}
        </button>
        {stats.ready > 0 && (
          <button onClick={exportSQL}>Export ready SQL ({stats.ready})</button>
        )}
        <input
          type="text"
          placeholder="Search by name or ID…"
          value={search}
          onChange={e => setSearch(e.target.value)}
          style={{ flex: 1, minWidth: 180 }}
        />
        <select value={filter} onChange={e => setFilter(e.target.value)}>
          <option value="all">All ({uniqueStudents.length})</option>
          <option value="ready">Ready ({stats.ready})</option>
          <option value="issues">Issues ({stats.issues})</option>
          <option value="pending">Pending ({stats.total - stats.analyzed - stats.errors})</option>
          <option value="error">Errors ({stats.errors})</option>
        </select>
      </div>

      {/* Student grid */}
      <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill, minmax(160px, 1fr))", gap: 10 }}>
        {filtered.map(student => {
          const r = results[student.id];
          const isAnalyzing = r?.status === STATUS.analyzing;
          const isDone = r?.status === STATUS.done;
          const isError = r?.status === STATUS.error;

          return (
            <div
              key={student.id}
              onClick={() => setSelected(selected?.id === student.id ? null : student)}
              style={{
                background: "var(--color-background-primary)",
                border: selected?.id === student.id
                  ? "2px solid var(--color-border-info)"
                  : "0.5px solid var(--color-border-tertiary)",
                borderRadius: "var(--border-radius-lg)",
                overflow: "hidden",
                cursor: "pointer",
                opacity: isAnalyzing ? 0.7 : 1,
              }}
            >
              {/* Photo */}
              <div style={{ position: "relative", aspectRatio: "1", background: "var(--color-background-secondary)", overflow: "hidden" }}>
                <img
                  src={thumbUrl(student.fileId)}
                  alt={student.name}
                  style={{ width: "100%", height: "100%", objectFit: "cover" }}
                  onError={e => { e.target.style.display = "none"; }}
                />
                {/* Status badge */}
                <div style={{
                  position: "absolute", top: 6, right: 6,
                  width: 10, height: 10, borderRadius: "50%",
                  background: isAnalyzing ? "var(--color-text-warning)"
                    : isDone && r.enrollmentReady ? "var(--color-text-success)"
                    : isDone && !r.enrollmentReady ? "var(--color-text-warning)"
                    : isError ? "var(--color-text-danger)"
                    : "var(--color-border-secondary)",
                  boxShadow: "0 0 0 2px var(--color-background-primary)"
                }} />
                {isAnalyzing && (
                  <div style={{
                    position: "absolute", inset: 0,
                    background: "rgba(0,0,0,0.3)",
                    display: "flex", alignItems: "center", justifyContent: "center",
                    fontSize: 12, color: "#fff", fontWeight: 500
                  }}>
                    Analyzing…
                  </div>
                )}
              </div>

              {/* Info */}
              <div style={{ padding: "8px 10px 10px" }}>
                <div style={{ fontSize: 12, fontWeight: 500, marginBottom: 2, direction: "rtl", textAlign: "right", overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>
                  {student.name}
                </div>
                <div style={{ fontSize: 11, color: "var(--color-text-secondary)", marginBottom: 6 }}>
                  {student.id}
                </div>

                {isDone && (
                  <div style={{
                    display: "inline-block",
                    fontSize: 10, fontWeight: 500,
                    padding: "2px 7px", borderRadius: 10,
                    background: qualityBg(r.quality),
                    color: qualityColor(r.quality)
                  }}>
                    {r.enrollmentReady ? "Ready" : r.quality || "Issues"}
                  </div>
                )}
                {isError && (
                  <div style={{ fontSize: 10, color: "var(--color-text-danger)" }}>Error loading</div>
                )}
                {!r && (
                  <button
                    onClick={e => { e.stopPropagation(); runSingle(student); }}
                    style={{ fontSize: 10, padding: "2px 8px" }}
                  >
                    Analyze
                  </button>
                )}
              </div>
            </div>
          );
        })}
      </div>

      {/* Detail panel */}
      {selected && results[selected.id]?.status === STATUS.done && (() => {
        const r = results[selected.id];
        return (
          <div style={{
            marginTop: "1.5rem",
            background: "var(--color-background-primary)",
            border: "0.5px solid var(--color-border-secondary)",
            borderRadius: "var(--border-radius-lg)",
            padding: "1.25rem",
          }}>
            <div style={{ display: "flex", gap: 16, alignItems: "flex-start" }}>
              <img
                src={thumbUrl(selected.fileId)}
                alt={selected.name}
                style={{ width: 100, height: 100, objectFit: "cover", borderRadius: "var(--border-radius-md)", flexShrink: 0 }}
              />
              <div style={{ flex: 1 }}>
                <div style={{ display: "flex", alignItems: "center", gap: 8, marginBottom: 6 }}>
                  <span style={{ fontSize: 15, fontWeight: 500, direction: "rtl" }}>{selected.name}</span>
                  <span style={{ fontSize: 12, color: "var(--color-text-secondary)" }}>{selected.id}</span>
                  <span style={{
                    fontSize: 11, fontWeight: 500, padding: "2px 8px", borderRadius: 10,
                    background: qualityBg(r.quality), color: qualityColor(r.quality)
                  }}>
                    {r.enrollmentReady ? "Enrollment ready" : "Needs review"}
                  </span>
                </div>
                <div style={{ fontSize: 13, color: "var(--color-text-secondary)", marginBottom: 10 }}>
                  {r.notes}
                </div>
                <div style={{ display: "grid", gridTemplateColumns: "repeat(3, 1fr)", gap: 8 }}>
                  {[
                    { label: "Faces detected", value: r.faceCount },
                    { label: "Lighting", value: `${r.lightingScore}/10` },
                    { label: "Clarity", value: `${r.clarity}/10` },
                    { label: "Face angle", value: r.faceAngle },
                    { label: "Quality", value: r.quality },
                    { label: "Photos available", value: selected.allFileIds?.length || 1 },
                  ].map(({ label, value }) => (
                    <div key={label} style={{ background: "var(--color-background-secondary)", borderRadius: "var(--border-radius-md)", padding: "6px 10px" }}>
                      <div style={{ fontSize: 10, color: "var(--color-text-secondary)" }}>{label}</div>
                      <div style={{ fontSize: 13, fontWeight: 500 }}>{value}</div>
                    </div>
                  ))}
                </div>
                {r.qualityIssues?.length > 0 && (
                  <div style={{ marginTop: 10, display: "flex", flexWrap: "wrap", gap: 4 }}>
                    {r.qualityIssues.map(issue => (
                      <span key={issue} style={{
                        fontSize: 10, padding: "2px 7px", borderRadius: 10,
                        background: "var(--color-background-danger)", color: "var(--color-text-danger)"
                      }}>
                        {issue.replace(/_/g, " ")}
                      </span>
                    ))}
                  </div>
                )}
              </div>
            </div>
          </div>
        );
      })()}

      <p style={{ marginTop: "1.5rem", fontSize: 12, color: "var(--color-text-secondary)" }}>
        Photos load from Google Drive thumbnails. Analysis uses Claude Vision to check face quality, lighting, clarity, and enrollment readiness. Use "Export ready SQL" to generate UPDATE statements for your database.
      </p>
    </div>
  );
}
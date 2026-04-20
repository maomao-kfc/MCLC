/* 文章数据 */

/* 文章数据 */
export const articleList = {
	// 中文文章（9篇完整填充）
	zh: [
		// 第1期 James Wolffsohn教授
		{
			id: 1,
			title: "MCLC 2026特邀嘉宾演讲集锦 第一期",
			subTitle: "James Wolffsohn教授主旨报告精彩回顾",
			enSubTitle: "Myopia Calculators（近视预测计算器）",
			publishTime: "2026-01-XX",
			author: "MCLC组委会",
			speaker: {
				name: "James Wolffsohn",
				title: "英国伯明翰阿斯顿大学视光学与听力学学院院长，泪膜与眼表学会（TFOS）主任",
				avatar: "/assets/image/guests/JamesWolffsohn.png"
			},
			content: [{
					type: "title",
					level: 2,
					text: "主旨报告内容回顾"
				},
				{
					type: "text",
					text: "在本次MCLC大会上，Wolffsohn教授以其深厚的循证医学理念，系统阐述了近视预测计算器这一前沿工具的科学基础与临床价值。Wolffsohn教授首先回顾了“循证实践”在眼科临床中的核心地位，它强调在个体化诊疗中，结合最佳科研证据、医师专业经验与患者意愿，做出审慎、科学的医疗决策。证据等级体系（循证医学金字塔）为此提供了清晰的评估框架。"
				},
				{
					type: "image",
					src: "/assets/image/article/JamesWolffsohnTu1.png",
					alt: "接触镜相关微生物角膜炎发病率统计",
					maxWidth: "90%"
				},
				{
					type: "text",
					text: "随着近视问题在全球范围内日益严峻，基于循证理念的国际近视研究所（IMI）自2015年成立以来，持续推动相关指南与白皮书的更新与普及。其在线白皮书平台提供多语言版本，已成为全球眼科工作者获取权威近视防控资源的重要窗口。"
				},
				{
					type: "image",
					src: "/assets/image/article/JamesWolffsohnTu2.png",
					alt: "接触镜相关微生物角膜炎发病率统计",
					maxWidth: "90%"
				},
				{
					type: "title",
					level: 3,
					text: "近视发展预测核心指标"
				},
				{
					type: "text",
					text: "一个关键问题常被提出：使用近视干预措施后，若眼轴仍在增长，这属于正常生理发育，还是干预效果不足？Wolffsohn教授指出，理解人眼从远视向正视发展的自然过程，是正确评估干预效果的基础。他强调，解答这一问题的前提，是精确预测近视的发展轨迹。多项研究已明确儿童近视的高风险指标，例如6岁儿童若屈光度 ≤ +0.75 D且>-0.50 D，即具有显著近视倾向。年龄、基线屈光度、父母近视史、高AC/A比、眼轴过长及周边屈光状态等，均为重要预测因素。如父母一方近视，风险增加约1.4倍；双方近视，风险升至约2.3倍；6岁儿童若眼轴长度>23.07mm且至少一方父母近视，其16岁前发生近视的风险可增加6.3倍。"
				},
				{
					type: "title",
					level: 3,
					text: "近视预测工具应用"
				},
				{
					type: "text",
					text: "为整合这些循证参数，Wolffsohn教授重点介绍了布莱恩·霍顿视觉研究院（BHVI）发布的第二代在线近视计算器。该工具能基于个体数据，模拟不同防控手段的长期效果。例如：一名6岁、屈光度为-0.50D的女孩，若无干预，至17岁时屈光度可能达-6.16D；若采用低浓度阿托品干预，则预计可降至-3.87D，延缓进展约37%。此外，该平台还可评估眼轴增长趋势及相关风险因素，为临床决策与患者教育提供直观、个性化的参考。"
				},
				{
					type: "image",
					src: "/assets/image/article/JamesWolffsohnTu3.png",
					alt: "接触镜相关微生物角膜炎发病率统计",
					maxWidth: "90%"
				},
				{
					type: "text",
					text: "此外，教授也简要列举了其他国际上可用的近视预测与管理在线工具，如Myopia care、CooperVision、Myopia Management and Orthokeratology Society、Ocumetra、PreMO及MyopiaProfile等平台，为从业者提供了多样化的辅助评估选择。"
				},
				{
					type: "title",
					level: 2,
					text: "独家专访深度回顾"
				},
				{
					type: "text",
					text: "在演讲之外，我们也有幸对Wolffsohn教授进行了简短专访。他结合临床实践，进一步分享了关于工具应用、行业未来及中国观察的深刻洞见。当被问及计算器的核心价值时，教授精辟地指出：“它们是极佳的沟通媒介。”"
				},
				{
					type: "list",
					list: [
						"对患者：工具能将复杂的参数转化为可视化故事，直观展示“如果什么都不做”与“积极干预”的未来差异，极大地增强了治疗信心与依从性。",
						"对医生：生成的评估报告不仅支持临床决策，更能让医生清晰追踪长期管理效果，“确信自己的工作正在产生积极影响”，形成一个评估—沟通—调整—建立信心的良性循环。",
						"对家庭：可分享的报告让家庭成员都能理解防控的重要性，形成共同参与的合力。"
					]
				},
				{
					type: "text",
					text: "对比中英两国的研究方向，教授特别肯定了中国近视防控的独特优势：系统性组织与大数据应用、“整体健康”的先进理念、政策聚焦“预防”的前瞻性视角。最后，谈及对本次大会的印象，教授由衷赞叹：“会场座无虚席，甚至有人始终站立聆听，这种对知识的热切渴望令人印象深刻。”他认为，中国眼健康从业者展现出的巨大学习热情和专业投入，是推动整个领域进步的最宝贵动力。"
				},
				{
					type: "title",
					level: 2,
					text: "参考文献与学习资料"
				},
				{
					type: "reference",
					list: [
						"Flitcroft DI, He M, Jonas JB, et al. IMI - Defining and Classifying Myopia: A Proposed Set of Standards for Clinical and Epidemiologic Studies. Invest Ophthalmol Vis Sci. 2019;60(3):M20-M30. doi:10.1167/iovs.18-25957",
						"Zadnik K, Sinnott LT, Cotter SA, et al. Prediction of Juvenile-Onset Myopia. JAMA Ophthalmol. 2015;133(6):683-689. doi:10.1001/jamaophthalmol.2015.0471",
						"McCullough SJ, O'Donoghue L, Saunders KJ. Six Year Refractive Change among White Children and Young Adults: Evidence for Significant Increase in Myopia among White UK Children. PLoS One. 2016;11(1):e0146332. doi:10.1371/journal.pone.0146332",
						"在线白皮书平台：https://myopiainstitute.org/imi-white-papers-clinical-summaries/",
						"BHVI：https://bhvi.org/"
					]
				}
			]
		},
		// 第2期 陈志教授
		{
			id: 2,
			title: "MCLC 2026特邀嘉宾演讲集锦 第二期",
			subTitle: "陈志教授专题演讲精彩回顾",
			enSubTitle: "Multicenter RCT of spectacles with diffusion optics technology (DOT) for myopia control in Chinese children — 2 year results",
			publishTime: "2026-01-31",
			author: "MCLC组委会",
			speaker: {
				name: "陈志",
				title: "副主任医师，副教授，硕士生导师，现任复旦大学附属眼耳鼻喉科医院接触镜和近视防控中心主任",
				avatar: "/assets/image/guests/ChenZhi.png"
			},
			content: [{
					type: "title",
					level: 2,
					text: "专题演讲内容回顾"
				},
				{
					type: "text",
					text: "陈志教授报告了一项关于点扩散光学设计（DOT）镜片用于中国儿童近视控制的多中心随机对照试验（RCT）的2年研究成果。近视已成为全球重大公共卫生挑战。2025年IMI近视管理白皮书指出，特殊光学镜片已广泛应用于儿童青少年近视防控，其疗效得到广泛验证。研究显示，视网膜对比度信号升高与近视进展相关，而DOT镜片通过特殊点扩散光学设计调节视网膜对比度，模拟更自然的视觉信号，从而抑制眼轴增长。该设计已在北美CYPRESS研究中证实有效——这项覆盖美国和加拿大14个中心、纳入256名6-10岁近视儿童的4年前瞻性RCT表明，DOT镜片能显著延缓近视进展与眼轴增长，且安全性良好。"
				},
				{
					type: "text",
					text: "然而陈志教授指出，亚洲人与白种人的眼球发育轨迹存在差异，DOT镜片在亚洲儿童中的防控效果尚未明确，因此在中国人群中进行疗效评估尤为必要。"
				},
				{
					type: "text",
					text: "为此，复旦大学附属眼耳鼻喉科医院携手爱尔眼科医院、天津市眼科医院、四川大学华西医院及中山大学中山眼科中心，共同开展了一项前瞻性、随机、对照、观察者单盲的多中心临床试验。研究覆盖中国东、南、西、北及中部五大区域，共纳入191名6-13岁儿童，按2：1比例随机分配至DOT组与单光镜片（SVL）组，随访24个月。主要终点为2年眼轴变化量，次要终点为6、12、18、24个月时的等效球镜变化量与眼轴变化量。"
				},
				{
					type: "text",
					text: "结果显示，两组基线特征无显著差异，具备可比性。陈志教授已在2024年国际近视眼大会（IMC）上汇报了该研究的12个月结果：DOT镜片可使绝对眼轴增长量每年减少0.26mm。分层分析显示，6-7岁、8-10岁、11-13岁各年龄段儿童均能从DOT配戴中获益。2年数据进一步表明，其控制效果未随时间减弱。"
				},
				{
					type: "text",
					text: "此外，他表示，DOT镜片的防控效果与DIMS、HAL等设计相当甚至略优，且其所纳入的儿童年龄更小、防控难度更高。配戴初期儿童可能需要数天适应，但一周后其适应性与普通单光镜片无异。"
				},
				{
					type: "text",
					text: "综上，陈志教授的研究表明在中国人群中，DOT镜片相较于标准单光镜片可显著减缓眼轴增长和近视进展，该结果与北美的研究一致，支持视网膜对比度信号降低可以减缓儿童近视进展的假说，为国内近视光学防控提供了新的循证选择。"
				},
				{
					type: "title",
					level: 2,
					text: "参考文献与学习资料"
				},
				{
					type: "reference",
					list: [
						"Rappon J, Chung C, Young G, et al. Control of myopia using diffusion optics spectacle lenses: 12-month results of a randomised controlled, efficacy and safety study (CYPRESS). Br J Ophthalmol. 2023;107(11):1709-1715. doi:10.1136/bjo-2021-321005",
						"Hariton E, Locascio JJ. Randomised controlled trials - the gold standard for effectiveness research: Study design: randomised controlled trials. BJOG. 2018;125(13):1716. doi:10.1111/1471-0528.15199",
						"Laughton D, Hill JS, McParland M, et al. Control of myopia using diffusion optics spectacle lenses: 4-year results of a multicentre randomised controlled, efficacy and safety study (CYPRESS). BMJ Open Ophthalmol. 2024;9(1):e001790. doi:10.1136/bmjophth-2024-001790",
						"Lam CSY, Tang WC, Tse DY, et al. Defocus Incorporated Multiple Segments (DIMS) spectacle lenses slow myopia progression: a 2-year randomised clinical trial. Br J Ophthalmol. 2020;104(3):363-368. doi:10.1136/bjophthalmol-2018-313739"
					]
				}
			]
		},
		// 第3期 Langis Michaud教授
		{
			id: 3,
			title: "MCLC 2026特邀嘉宾演讲集锦 第三期",
			subTitle: "Langis Michaud教授专题演讲精彩回顾",
			enSubTitle: "How to improve myopia control with optical devices : understanding the retinal response to visual signal",
			publishTime: "2026-02-06",
			author: "MCLC组委会",
			speaker: {
				name: "Langis Michaud",
				title: "蒙特利尔大学接触镜科主任、美国眼视光学会资深会员",
				avatar: "/assets/image/guests/Langis.png"
			},
			content: [{
					type: "title",
					level: 2,
					text: "专题演讲内容回顾"
				},
				{
					type: "text",
					text: "Langis Michaud教授围绕“视网膜对视觉信号的应答”核心主题，系统阐述了视网膜调控眼球生长的底层机制，并基于该机制提出光学镜片优化近视防控效果的实践路径。"
				},
				{
					type: "text",
					text: "眼球生长主要由视网膜调控，其通过解析视觉信号激活视网膜—巩膜生化通路以驱动正视化过程。该系统的功能健全是维持正视状态的关键，而近视的发生则与其部分功能缺失有关。黄斑区聚焦异常（类似形觉剥夺）、周边视网膜离焦，均会影响屈光发育。"
				},
				{
					type: "text",
					text: "从视网膜功能分化来看，光感受器可通过响应光线波长、物体空间朝向与运动方向等不同视觉刺激实现功能特化，其将光能量传递至15种不同亚型的双极细胞后，完成视觉信息的特异性提取与编码，再通过两条核心通路完成信号传输：明暗通路主要负责图像处理，色觉通路则参与模糊信息解析，两者协同实现视网膜对图像信息的精准解析。"
				},
				{
					type: "text",
					text: "视网膜对图像信息的解析主要依赖对比度、空间频率两大维度，其敏感通路可同时编码光增强与减弱信号，是感知物体与空间对比度、形状及边缘信息的关键。其中，双极细胞的“中心—周边拮抗”感受野结构是识别对比度与边缘信息的基础。"
				},
				{
					type: "title",
					level: 3,
					text: "近视发生的核心机制"
				},
				{
					type: "list",
					list: [
						"明通路（ON通路）与暗通路（OFF通路）对眼球生长具有相反的调控作用：抑制明通路或刺激暗通路会促进眼轴生长。近视在某种程度上可被视为明通路的功能缺陷状态。从遗传学基础来看，明/暗通路的病理性激活是驱动高度近视的关键，其根源与影响视色素基因的遗传突变（特定单倍型）相关，约25%的人群携带此类突变，高度近视患病风险为普通人群的3倍。",
						"色像差（包括纵向与横向色差）为视网膜提供了辨别离焦性质（正/负）的关键信号，进而指导发出“生长”或“停止生长”的指令。然而，近视眼对该信号的敏感性常减弱。此外，视网膜具备神经适应性，这可能是所有光学干预手段疗效随时间推移出现平台效应的重要原因。"
					]
				},
				{
					type: "title",
					level: 3,
					text: "光学镜片优化防控效果的核心策略"
				},
				{
					type: "text",
					text: "基于上述视网膜信号应答与近视调控的核心机制， Langis Michaud教授结合临床研究经验，提出光学镜片优化近视防控效果的具体策略，明确光学干预的核心原则——通过精准调控视网膜离焦状态、激活明通路功能，改善视网膜信号应答，进而抑制眼球异常生长。"
				},
				{
					type: "title",
					level: 4,
					text: "透镜光学特性的应用原理"
				},
				{
					type: "list",
					list: [
						"透镜的屈光变化可改变目标物像大小，进而影响视网膜感受野应答：刺激局限于感受野中心区时，视网膜神经反应增强；刺激波及周边区时，因周边拮抗机制导致反应减弱；刺激超出周边区时，则无明显神经反应。",
						"离焦梯度可干扰明/暗刺激识别过程，且对ON与OFF通路的影响存在差异，其可扩大ON通路感受野、缩小OFF通路感受野，解释了离焦对明、暗刺激感知的差异化效应。",
						"当物像尺寸与亮度不变时，ON与OFF通路无法可靠的区分正离焦与负离焦，但可精准传递离焦程度和物像放大率的变化信号，这也解释了为何正镜片相较于负镜片，更能产生积极的近视矫治效果——放大率是影响视网膜信号应答的关键因素，优于离焦符号的影响。"
					]
				},
				{
					type: "title",
					level: 4,
					text: "光学镜片的临床应用规范"
				},
				{
					type: "list",
					list: [
						"避免使用单光镜片，单光镜片无法改善周边视网膜负球面像差及鼻侧视网膜垂直方向离焦，难以有效调控视网膜信号应答。",
						"密切随访患者屈光状态，及时调整矫正方案，若中心视力持续模糊，易引发形觉剥夺，加速近视进展。",
						"严禁近视欠矫的眼镜，屈光度变化超过0.25D时，需及时更换镜片，确保视网膜获得清晰视觉刺激，避免信号通路紊乱。",
						"强调全天佩戴光学矫正镜片，确保离焦刺激持续作用于视网膜，维持稳定的信号调控效应。",
						"微透镜镜片中优先选择正镜片，其可放大视觉信号尺寸，更易激活ON通路。",
						"优选中心视远区设计的镜片，此类镜片可提供更高的正球面像差和更大的放大率，更符合视网膜调控需求。",
						"阿托品可通过扩大瞳孔直径，增强离焦与对比度的剂量效应。",
						"已近视患者或需要更高的剂量效应或不同的光学刺激，神经适应可发生在干预第1年后，此时需要及时调整策略。"
					]
				},
				{
					type: "text",
					text: "综上，Langis Michaud教授的汇报表明近视的光学管理本质是向视网膜传递精确调控信号。通过深入理解视网膜的信号处理机制与像差的作用，并据此优化光学镜片的设计与应用策略，可在将来实现更精准、有效的近视控制。"
				},
				{
					type: "title",
					level: 2,
					text: "参考文献与学习资料"
				},
				{
					type: "reference",
					list: [
						"Wallman J, Winawer J. Homeostasis of eye growth and the question of myopia. Neuron. 2004;43(4):447-468. doi:10.1016/j.neuron.2004.08.008",
						"Poudel S, Jin J, Rahimi-Nasrabadi H, et al. Contrast Sensitivity of ON and OFF Human Retinal Pathways in Myopia. J Neurosci. 2024;44(3):e1487232023. Published 2024 Jan 17. doi:10.1523/JNEUROSCI.1487-23.2023",
						"Yang Y, Lee D, Gettinger K, et al. Mechanisms Underlying Myopia Progression from Visual Signaling to Metabolic Remodeling in Retina. JMA J. 2025;8(4):1031-1038. doi:10.31662/jmaj.2025-0268",
						"Neitz J, Neitz M. Diffusion Optics Technology (DOT): A Myopia Control Spectacle Lens Based on Contrast Theory. Transl Vis Sci Technol. 2024;13(10):42. doi:10.1167/tvst.13.10.42"
					]
				}
			]
		},
		// 第4期 周行涛教授
		{
			id: 4,
			title: "MCLC 2026特邀嘉宾演讲集锦 第四期",
			subTitle: "周行涛教授主旨报告精彩回顾",
			enSubTitle: "AI-empowered Full-life Cycle Eye Health",
			publishTime: "2026-XX-XX",
			author: "MCLC组委会",
			speaker: {
				name: "周行涛",
				title: "主任医师，教授，博士生导师，现任复旦大学附属眼耳鼻喉科医院院长",
				avatar: "/assets/image/guests/zhouXingTao.png"
			},
			content: [{
					type: "title",
					level: 2,
					text: "主旨报告内容回顾"
				},
				{
					type: "text",
					text: "周行涛教授系统展示了其如何将人工智能技术深度融合于近视防治的全周期管理，从病因探索、早期筛查、风险预警到手术规划与疗效评估，构建起一套贯穿“筛-诊-治-防”的数智化创新体系。"
				},
				{
					type: "text",
					text: "我国近视防控形势严峻，青少年近视不断攀升且呈现低龄化、重度化、发展快和程度深的趋势。周行涛教授团队联合多学科，通过覆盖超250万人的大队列研究，将近视防控的关口前移至全生命周期早期阶段，揭示妊娠期高血压疾病与子代高度屈光不正风险的显著关联性，提示预防高度近视应从孕期开始关注，这对于高度近视防治具有创新性且重要的指导意义。"
				},
				{
					type: "title",
					level: 3,
					text: "AI在早期筛查与风险预警的应用"
				},
				{
					type: "text",
					text: "在早期筛查与风险预警领域，团队研发了多项AI工具以实现精准防控。通过构建AI全眼屈光模型，可在免散瞳条件下准确测算儿童生理性远视储备，ACC高达0.953；同时，集成多源数据建立的近视风险预测模型，即学校近视筛查AI复合一体机与云屈光档案系统，能提供个性化预警与防控建议。此外，团队利用AI影像技术实现儿童眼底豹纹密度自动定量分析，证实其与眼轴快速增长显著相关，为近视进展监测提供了新颖的无创生物标志物。"
				},
				{
					type: "title",
					level: 3,
					text: "AI在屈光手术精准化的应用"
				},
				{
					type: "text",
					text: "在屈光手术的精准化方面，AI技术同样发挥了核心作用。针对ICL植入术，团队利用大数据和机器学习模型，对影响术后拱高和晶体尺寸选择的18个特征参数进行分析，建立的预测模型使ICL尺寸选择准确率超过82%。对于SMILE手术，团队创新性地开发了基于MatLab的自动化算法，能够客观、精确地测量术后有效光学区的大小和形态，解决了传统方法重复性差的临床难题。"
				},
				{
					type: "text",
					text: "团队还致力于构建完整的国产化“AI+眼科”临床应用生态。研发的AI角膜塑形镜验配平台可辅助OK镜验配，提升效率；国产数智化眼前节分析系统针对圆锥角膜构建早筛模型，其诊断灵敏度与特异度提升至98%以上；国产数智化眼前节分析系统——AI辅助ICL个性化设计平台成功实现专利转化与产业化。这些成果形成了“AI术前评估-手术规划-术后随访”的闭环服务，有力推动了国产眼科设备与耗材产业的升级。"
				},
				{
					type: "text",
					text: "综上，周行涛教授团队通过将人工智能深度植入近视防治管理全周期的各个环节，从病因研究、早期筛查、手术规划到生态构建，不仅显著提升了诊疗的精准性与效率，更创新了近视全周期管理的模式。"
				},
				{
					type: "title",
					level: 2,
					text: "参考文献与学习资料"
				},
				{
					type: "reference",
					list: [
						"Li M, Huang C, Yang W, et al. Evaluation of Hypertensive Disorder of Pregnancy and High Refractive Error in Offspring During Childhood and Adolescence. JAMA Netw Open. 2023;6(4):e238694. Published 2023 Apr 3. doi:10.1001/jamanetworkopen",
						"Wei R, Li J, Yang W, et al. ASSOCIATION OF TESSELLATION DENSITY WITH PROGRESSION OF AXIAL LENGTH AND REFRACTION IN CHILDREN: An Artificial Intelligence-Assisted 4-Year Study. Retina. 2024;44(3):527-536. doi:10.1097/IAE.0000000000003991",
						"Shen Y, Wang L, Jian W, et al. Big-data and artificial-intelligence-assisted vault prediction and EVO-ICL size selection for myopia correction. Br J Ophthalmol. 2023;107(2):201-206. doi:10.1136/bjophthalmol-2021-319618"
					]
				}
			]
		},
		// 第5期 Takahiro Hiraoka教授
		{
			id: 5,
			title: "MCLC 2026特邀嘉宾演讲集锦 第五期",
			subTitle: "Takahiro Hiraoka教授主旨报告精彩回顾",
			enSubTitle: "Myopia control and microbial keratitis associated with orthokeratology in Japan",
			publishTime: "2026-XX-XX",
			author: "MCLC组委会",
			speaker: {
				name: "Takahiro Hiraoka",
				title: "副教授，筑波大学眼科医学研究所",
				avatar: "/assets/image/guests/Takahiro.png"
			},
			content: [{
					type: "title",
					level: 2,
					text: "主旨报告内容回顾"
				},
				{
					type: "text",
					text: "Takahiro Hiraoka教授系统回顾了其团队在角膜塑形镜（OK镜）领域长达十余年的研究成果，从长期疗效、联合治疗、镜片沉积物风险到感染并发症的流行病学数据，全面呈现了OK镜在临床应用中卓越的控制效果与不容忽视的安全管理。"
				},
				{
					type: "title",
					level: 3,
					text: "OK镜的近视控制长期疗效"
				},
				{
					type: "text",
					text: "关于OK镜的近视控制效果，Hiraoka教授展示了其团队2018年发表的十年观察性研究，该研究对比了104眼持续配戴OK镜与78眼配戴软性接触镜的患者，结果证实长期OK镜配戴能够持续有效地抑制眼轴增长。在联合治疗方面，多项研究显示OK镜联合0.01%阿托品可进一步增强眼轴控制效果。关于治疗区直径对眼轴增长的影响，研究表明较小的光学区（5mm）比较大光学区（6mm）设计能带来更好的控制效果。此外，OK镜的联合应用以及其在屈光参差治疗中的价值也在临床研究中得到验证。"
				},
				{
					type: "title",
					level: 3,
					text: "OK镜的安全管理与感染防控"
				},
				{
					type: "text",
					text: "Hiraoka教授警示，OK镜的安全相关问题必须引起高度重视。其团队对174片佩戴数年的OK镜片进行的沉积物分析显示，OK镜表面易沉积蛋白质和脂质，尤其是反转弧区域，这会使生物膜形成量增加6倍。此外这些沉积物会显著增加棘阿米巴的黏附量。针对顽固沉积物，Hiraoka教授推荐定期使用含次氯酸钠的强效清洁剂，其不仅能够有效清除沉积物、平滑镜片表面，还具备广谱消毒作用，对细菌、真菌、棘阿米巴乃至新冠病毒均有杀灭效果，在欧洲和中国已获批作为消毒剂使用。"
				},
				{
					type: "text",
					text: "关于OK镜相关微生物角膜炎的发生率，Hiraoka教授引用了多项国际流行病学研究数据。美国2013年的研究显示其发生率为7.7/万人年，而俄罗斯2021年的研究显示其发生率为4.9-5.3/万人年，与日抛型软镜相当。Hiraoka教授团队2025年最新发表的多中心研究纳入了1438例OK镜配戴者，计算出的感染发生率为5.4/万人年，与俄罗斯的研究结果相近，证实近年来感染率呈现下降趋势。Hiraoka教授同时分享了四例典型的感染病例，病原体包括铜绿假单胞菌和棘阿米巴，所有病例最终均停用OK镜并转为日抛软镜。"
				},
				{
					type: "text",
					text: "综上，Takahiro Hiraoka教授的报告揭示：角膜塑形术的近视控制效果已获长期循证支持，但其临床价值的实现，必须以科学护理构筑的安全防线为前提。"
				},
				{
					type: "title",
					level: 2,
					text: "参考文献与学习资料"
				},
				{
					type: "reference",
					list: [
						"Hiraoka T, Sekine Y, Okamoto F, Mihashi T, Oshika T. Safety and efficacy following 10-years of overnight orthokeratology for myopia control. Ophthalmic Physiol Opt. 2018;38(3):281-289. doi:10.1111/opo.12460",
						"Hiraoka T, Yoshimitsu M, Santodomingo-Rubido J, Kondo H, Oshika T. A novel quantitative evaluation of deposits adhered to worn orthokeratology contact lenses. Jpn J Ophthalmol. 2021;65(6):855-863. doi:10.1007/s10384-021-00873-1",
						"Hiraoka T, Matsumura S, Hori Y, Kamiya K, Miyata K, Oshika T. Incidence of microbial keratitis associated with overnight orthokeratology: a multicenter collaborative study. Jpn J Ophthalmol. 2025;69(1):139-143. doi:10.1007/s10384-024-01137-4"
					]
				}
			]
		},
		// 第6期 吕帆教授
		{
			id: 6,
			title: "MCLC 2026特邀嘉宾演讲集锦 第六期",
			subTitle: "吕帆教授主旨报告精彩回顾",
			enSubTitle: "High Myopia and Myopia Control",
			publishTime: "2026-XX-XX",
			author: "MCLC组委会",
			speaker: {
				name: "吕帆",
				title: "教授，博士生导师，温州医科大学眼视光医学部主任",
				avatar: "/assets/image/guests/LvFan.png"
			},
			content: [{
					type: "title",
					level: 2,
					text: "主旨报告内容回顾"
				},
				{
					type: "text",
					text: "吕帆教授从全球流行病学趋势出发，系统阐释了高度近视研究的核心命题及其对临床防控的深刻启示。"
				},
				{
					type: "text",
					text: "近视已成为全球性的公共卫生挑战。据预测，到2050年，全球高度近视人口比例将增至10%。这一趋势促使学界重新审视近视的本质属性——它正被定义为一种“慢性疾病”。其理由在于：近视具有进展性本质，高度近视可发展为病理性近视，引发不可逆的视力损伤；全球高患病率带来了重大公共卫生挑战；而早期干预已被证明能有效延缓进展。近视防控的终极目标，正是防止近视发生、减缓近视进展，从而避免高度近视及其伴随的视觉损伤。"
				},
				{
					type: "text",
					text: "在发病机制层面，近视的发生是遗传与环境因素交互作用的结果。GWAS研究已累计发现284个近视易感基因，却只能解释不足10%的近视人群发病。吕帆教授团队近期通过大样本发现了全新的早发型高度近视致病基因BSG，为理解高度近视的遗传基础提供了新线索。值得注意的是，部分易感基因显著富集于核心功能模块，提示这些模块可能是遗传与环境因素互作的关键环节。"
				},
				{
					type: "text",
					text: "基于温州百万学生近视队列研究，吕帆教授团队呈现了中国各年龄段近视与高度近视的患病率分布，且发现高度近视在各个年级持续增加。这一流调证据揭示了近视防控的严峻形势，尤其高度近视的累积效应值得高度警惕。"
				},
				{
					type: "title",
					level: 3,
					text: "高度近视与病理性近视的核心区别"
				},
				{
					type: "text",
					text: "吕教授深入阐述了高度近视与病理性近视的本质区别。病理性近视是主要致盲原因，其眼底改变包括后巩膜葡萄肿、视网膜下新生血管、视网膜萎缩变性与出血、视网膜脱离等。随着眼轴延长，眼底发生持续性萎缩变性，视功能严重受损。荟萃分析显示，近视性黄斑变性已成为中国、日本、丹麦和荷兰永久失明的主要原因。"
				},
				{
					type: "text",
					text: "从正常眼底到病理性近视，存在一条可追溯的演进路径：正视或低中度近视表现为正常眼底；随着眼轴增长，出现豹纹状眼底；进入单纯高度近视阶段；进一步发展为弥漫性萎缩；最终进入斑片状萎缩、黄斑萎缩及其并发症阶段。这一连续谱系提示，关键问题在于识别具备何种特征的近视者将有高概率进入病理性阶段，从而实施精准干预。"
				},
				{
					type: "text",
					text: "为揭示单纯性高度近视阶段已存在的功能损伤，吕帆教授团队采用多种先进技术开展研究。微视野计检测发现，单纯性高度近视患者在无任何并发症出现时，就已发生视网膜光敏感度下降。自适应光学眼底照相机显示，高度近视眼与正视眼相比，视锥细胞密度降低、细胞间距增大，提示光感受器细胞已发生损伤和凋亡。OCTA研究进一步揭示，病理性近视脉络膜极度萎缩时，深层视网膜毛细血管会启动对外层视网膜的代偿供血。脉络膜血管指数（CVI）当眼轴超过阈值时显著下降，且与视力呈负相关，可能作为监测病理性近视进展的可靠生物学指标。"
				},
				{
					type: "text",
					text: "基于上述研究证据，吕帆教授提出高度近视防控的核心策略：重点关注近视前期，延缓发病、降低发病率；低度近视阶段，控制进展、降低高度近视风险；高度近视阶段，减少不可逆的视力丧失和眼底并发症。"
				},
				{
					type: "text",
					text: "综上，吕帆教授的报告最终揭示：近视防控绝非单纯的度数管理，而是贯穿生命周期的慢性病系统工程。近视防控，任重而道远。"
				},
				{
					type: "title",
					level: 2,
					text: "参考文献与学习资料"
				},
				{
					type: "reference",
					list: [
						"Holden BA, Fricke TR, Wilson DA, et al. Global Prevalence of Myopia and High Myopia and Temporal Trends from 2000 through 2050. Ophthalmology. 2016;123(5):1036-1042. doi:10.1016/j.ophtha.2016.01.006",
						"Xu L, Zhuang Y, Zhang G, et al. Design, methodology, and baseline of whole city-million scale children and adolescents myopia survey (CAMS) in Wenzhou, China. Eye Vis (Lond). 2021;8(1):31. doi:10.1186/s40662-021-00255-1",
						"Haarman AEG, Enthoven CA, Tideman JWL, et al. The Complications of Myopia: A Review and Meta-Analysis. Invest Ophthalmol Vis Sci. 2020;61(4):49. doi:10.1167/iovs.61.4.49"
					]
				}
			]
		},
		// 第7期 Pauline Kang教授
		{
			id: 7,
			title: "MCLC 2026特邀嘉宾演讲集锦 第七期",
			subTitle: "Pauline Kang教授大会发言精彩回顾",
			enSubTitle: "The Influence of Visual Behaviour on Myopia Control Outcomes",
			publishTime: "2026-XX-XX",
			author: "MCLC组委会",
			speaker: {
				name: "Pauline Kang",
				title: "悉尼新南威尔士大学视光学与视觉科学学院副教授",
				avatar: "/assets/image/guests/Pauline.png"
			},
			content: [{
					type: "title",
					level: 2,
					text: "主旨报告内容回顾"
				},
				{
					type: "text",
					text: "Pauline Kang教授围绕视觉行为对近视控制的影响展开系统论述。她指出，近视的风险因素可划分为固定因素与可改变因素两大类，其中户外光照暴露与近距离工作作为核心的可改变因素，正成为近视防控干预的关键靶点。"
				},
				{
					type: "title",
					level: 3,
					text: "户外光照暴露的近视保护作用"
				},
				{
					type: "text",
					text: "户外光照暴露已被大量研究证实对近视发生具有保护作用，并可能延缓近视进展。Pauline Kang教授强调，起保护作用的是光照本身而非体力活动，其机制尚未完全阐明，目前认为与多巴胺释放及昼夜节律调节相关。在测量方法上，主观问卷易受偏倚影响，可穿戴传感器等客观工具正成为研究新标准。她介绍了ROAM研究、Recess Outside Classroom Trial及上海STORM研究等代表性工作，这些研究通过客观光照监测证实了户外暴露的干预价值。研究提示，光照的时间特征、光谱组成与强度均是影响效应的关键参数。基于现有证据，她建议临床实践中应鼓励儿童每日至少2小时户外光照暴露，可分次累积，并做好紫外线防护。"
				},
				{
					type: "title",
					level: 3,
					text: "近距离工作与近视的关联"
				},
				{
					type: "text",
					text: "关于近距离工作与近视的关联，Pauline Kang教授指出，更高的教育程度与近视相关，但阅读写作等近距离工作与近视的直接联系仍存争议。部分研究显示持续时长过长与过近用眼距离可能增加风险。随着数字设备普及，儿童个人娱乐及学校教育中的屏幕时间显著增加，这可能带来更近的用眼距离与更长的近距离注视时长，从而改变视觉行为模式。COVID-19疫情期间的行为与环境变化则进一步加剧了近视进展。"
				},
				{
					type: "text",
					text: "Pauline Kang教授强调，对近距离用眼行为的准确测量同样依赖客观工具。她介绍了多中心MENOK研究采用可穿戴传感器Vivior连续记录儿童视觉行为，初步结果显示近距离注视时长与近视进展可能存在正向关联，但需更长随访周期验证。该研究纳入的近视儿童接受包括近视防控镜片、接触镜、低浓度阿托品及联合治疗等多种干预方案，为探索不同治疗背景下视觉行为的影响提供了丰富数据。"
				},
				{
					type: "text",
					text: "综上，Pauline Kang教授指出深入理解户外光照与近距离工作对视网膜调控信号的影响机制，将为优化近视控制策略提供重要依据。客观监测技术的应用与多中心研究的推进，正推动视觉行为研究从经验判断走向循证实践，为临床医生指导儿童视觉行为、提升近视控制效果开辟了新路径。"
				},
				{
					type: "title",
					level: 2,
					text: "参考文献与学习资料"
				},
				{
					type: "reference",
					list: [
						"Wu PC, Chen CT, Lin KK, et al. Myopia Prevention and Outdoor Light Intensity in a School-Based Cluster Randomized Trial. Ophthalmology. 2018;125(8):1239-1250. doi:10.1016/j.ophtha.2017.12.011",
						"Chen J, Wang J, Qi Z, et al. Smartwatch Measures of Outdoor Exposure and Myopia in Children. JAMA Netw Open. 2024;7(8):e2424595. doi:10.1001/jamanetworkopen.2024.24595",
						"Ha A, Lee YJ, Lee M, et al. Digital Screen Time and Myopia: A Systematic Review and Dose-Response Meta-Analysis. JAMA Netw Open. 2025;8(2):e2460026. doi:10.1001/jamanetworkopen.2024.60026"
					]
				}
			]
		},
		// 第8期 李仕明教授
		{
			id: 8,
			title: "MCLC 2026特邀嘉宾演讲集锦 第八期",
			subTitle: "李仕明教授大会发言精彩回顾",
			enSubTitle: "Cry1 Deficiency Triggers Circadian Rhythm Disruption and Drives Myopia Pathogenesis",
			publishTime: "2026-XX-XX",
			author: "MCLC组委会",
			speaker: {
				name: "李仕明",
				title: "首都医科大学附属北京同仁医院眼科主任医师、教授、博士生导师",
				avatar: "/assets/image/guests/LiShiMing.png"
			},
			content: [{
					type: "title",
					level: 2,
					text: "主旨报告内容回顾"
				},
				{
					type: "text",
					text: "李仕明教授系统报告了其团队在昼夜节律与近视发病机制方面的系列研究成果，揭示了生物钟基因Cry1在近视发生中的核心作用。"
				},
				{
					type: "text",
					text: "近视已成为全球性公共卫生挑战，预计到2050年全球近视人口将达47.58亿。其病因受遗传与环境因素共同影响，其中睡眠不足、夜间光照等节律相关因素日益受到关注。李仕明教授团队基于“安阳儿童眼病研究”发现，睡眠时间增加与儿童近视程度降低显著相关，且在女孩中更为明显，提示昼夜节律紊乱可能参与近视发生。"
				},
				{
					type: "text",
					text: "动物实验进一步验证了这一假说。李仕明教授团队构建异常光暗周期小鼠模型，发现节律紊乱组小鼠出现明显近视漂移和眼轴延长，巩膜组织排列疏松，I型胶原纤维比例减少。转录组学分析富集到多巴胺、γ-氨基丁酸等神经递质信号通路，提示节律紊乱通过影响视网膜神经递质系统参与近视调控。"
				},
				{
					type: "text",
					text: "在众多生物钟基因中，Cry1引起团队特别关注。Cry1基因敲除小鼠出现自发性近视，表现为屈光向近视漂移、眼轴延长、巩膜厚度变薄，同时视网膜功能发生改变。转录组分析揭示Cry1缺失涉及细胞外基质重塑、氧化应激和炎症激活等途径。"
				},
				{
					type: "title",
					level: 3,
					text: "核心发病机制"
				},
				{
					type: "text",
					text: "机制研究阐明，CRY1蛋白可结合并抑制腺苷酸环化酶，从而降低细胞内cAMP水平。Cry1缺失导致巩膜cAMP含量升高，激活PKA信号通路，进而上调HIF-1α和α-SMA表达，促进成纤维细胞转分化，最终引起巩膜重塑和眼轴延长。在人胚眼巩膜成纤维细胞中，低氧环境下CRY1表达下降，而上调CRY1表达则可逆转这一过程。"
				},
				{
					type: "text",
					text: "李仕明教授的研究系统揭示了Cry1通过cAMP通路影响巩膜重塑、导致近视发生的分子机制，为近视防控开辟了新靶点。基于此，他提出“节律优化”策略：开发Cry1激动剂、优化光环境（白天增加户外光暴露、夜间减少人造光），以及开展节律基因筛查与多维评估，实现近视风险的个体化预警与精准干预。"
				},
				{
					type: "title",
					level: 2,
					text: "参考文献与学习资料"
				},
				{
					type: "reference",
					list: [
						"Wei SF, Li SM, Liu L, et al. Sleep Duration, Bedtime, and Myopia Progression in a 4-Year Follow-up of Chinese Children: The Anyang Childhood Eye Study. Invest Ophthalmol Vis Sci. 2020;61(3):37. doi:10.1167/iovs.61.3.37",
						"Bai WL, Wang MJ, Gan JH, et al. Circadian rhythm disruption induces myopia in mice. Exp Eye Res. 2026;264:110823. doi:10.1016/j.exer.2025.110823",
						"Allada R, Bass J. Circadian Mechanisms in Medicine. N Engl J Med. 2021;384(6):550-561. doi:10.1056/NEJMra1802337"
					]
				}
			]
		},
		// 第9期 Debarun Dutta教授
		{
			id: 9,
			title: "MCLC 2026特邀嘉宾演讲集锦 第九期",
			subTitle: "Debarun Dutta教授大会发言精彩回顾",
			enSubTitle: "Safety first: Antimicrobial strategies for CL-related infections: recent updates",
			publishTime: "2026-XX-XX",
			author: "MCLC组委会",
			speaker: {
				name: "Debarun Dutta",
				title: "博士，现任英国接触镜协会（BCLA）学术委员会主席",
				avatar: "/assets/image/guests/DebarunDutta.png"
			},
			content: [{
					type: "video",
					src: "/assets/video/特邀嘉宾演讲集锦演示.mp4",
				},
				{
					type: "title",
					level: 2,
					text: "主旨报告内容回顾"
				},
				{
					type: "text",
					text: "Debarun Dutta教授围绕接触镜相关感染的安全性议题，系统梳理了当前软性接触镜与角膜塑形镜在临床应用中的感染风险、抗菌策略及近视控制对眼表的影响，为临床安全实践提供了重要指导。"
				},
				{
					type: "text",
					text: "接触镜相关性角膜炎的发病率是衡量其安全性的核心指标。基于多变量分析的流行病学数据显示，接触镜相关微生物角膜炎的发生率为13.9例/万人年，范围在0.9至34.5例/万人年。关于过夜配戴硬性镜片及角膜塑形镜的数据相对有限，严重微生物角膜炎病例并不多见。在儿童配戴软性接触镜方面，研究显示微生物角膜炎发病率为2.7例/万人年，有症状的角膜浸润发生率为42例/万人年，总体而言儿童不良事件发生率与成人相当或更低。"
				},
				{
					type: "text",
					text: "抗生素耐药性问题为接触镜相关感染的治疗增添了新的挑战。中国2013至2015年的住院患者数据显示，约27.45%的患者存在抗生素耐药性感染或耐药菌定植，其中单药耐药占11.68%，多药耐药占15.77%。这一严峻背景凸显了在接触镜领域发展有效抗菌策略的迫切性。"
				},
				{
					type: "text",
					text: "针对如何抑制微生物污染、降低镜片相关不良事件，Dutta教授系统介绍了多种抗菌策略。研究表明，影响细菌黏附的因素包括镜片材料、表面特性、配戴方式及护理方案。近年来，抗菌接触镜及镜盒的研发取得了积极进展，但临床应用仍需进一步验证。对于角膜塑形镜，文献检索显示超过75%的相关安全性研究发表于过去十年，而感染相关研究中近半数集中于近十年，但多数为屈光矫正或近视控制效果研究，感染防控方面缺乏重大突破。2005至2008年间有29篇报告，多数来自中国。"
				},
				{
					type: "image",
					src: "/assets/image/article/DebarunDutta1.png",
					maxWidth: "90%"
				},
				{
					type: "text",
					text: "Dutta教授指出，许多早期棘阿米巴角膜炎病例发生在角膜塑形镜市场尚未规范之时，当时存在使用自来水冲洗镜片等不当操作。当前，提升安全性的关键在于持续开展医生与验光师培训、建立交流网络与导师指导机制、规范配戴程序并要求患者现场演示操作、监督使用推荐护理液、严格执行镜盒清洁与定期更换、以及建立24小时分诊热线等。他强调，禁止使用自来水、充分洗手与擦干双手是预防感染的基础。"
				},
				{
					type: "image",
					src: "/assets/image/article/DebarunDutta2.png",
					maxWidth: "90%"
				},
				{
					type: "text",
					text: "关于近视控制对眼表的不良影响，Dutta教授展示了一项系统综述结果。该综述从PubMed、Scopus及Web of Science筛选900余篇文献，最终纳入109篇临床试验报告，51项研究进入分析，共涉及8113名患者。结果显示，角膜塑形镜对眼表影响最大，主要表现为非侵入性泪膜破裂时间降低、睑板腺功能障碍发生率增高；软性接触镜对眼表参数有中度影响，主要表现为角膜染色和泪膜不稳定性增加；阿托品影响泪液分泌与稳定性；框架眼镜则几乎不产生影响。需要注意的是，研究方法存在显著差异，且软性接触镜相关研究中商业资助比例较高，可能引入偏倚。"
				},
				{
					type: "text",
					text: "综上所述，Dutta教授的报告最终指向一个核心结论：接触镜相关安全性的提升，需要从抗菌策略研发、规范护理流程、强化患者教育、定期随访监测等多维度协同推进。在当前抗生素耐药性日益严峻的背景下，安全意识的强化与科学护理的普及，是保障近视控制手段可持续应用的前提。"
				},
				{
					type: "title",
					level: 2,
					text: "参考文献与学习资料"
				},
				{
					type: "reference",
					list: [
						"Szczotka-Flynn LB, Shovlin JP, Schnider CM, et al. American Academy of Optometry Microbial Keratitis Think Tank. Optom Vis Sci. 2021;98(3):182-198. doi:10.1097/OPX.0000000000001664",
						"Bullimore MA, Richdale K. Incidence of Corneal Adverse Events in Children Wearing Soft Contact Lenses. Eye Contact Lens. 2023;49(5):204-211. doi:10.1097/ICL.0000000000000976",
						"Dutta D, Cole N, Willcox M. Factors influencing bacterial adhesion to contact lenses. Mol Vis. 2012;18:14-21."
					]
				}
			]
		}
	].reverse(),
	// 英文文章预留，按需补充
	en: []
}
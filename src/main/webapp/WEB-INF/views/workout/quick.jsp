<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>즉시 운동하기</title>

    <!-- Tailwind CSS CDN -->
    <script src="https://cdn.tailwindcss.com"></script>
    <!-- FontAwesome CDN -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <!-- Vue.js 3 CDN -->
    <script src="https://unpkg.com/vue@3/dist/vue.global.js"></script>

    <script>
        tailwind.config = {
            theme: {
                extend: {
                    colors: {
                        brand: {
                            DEFAULT: '#43996A',
                            dark: '#357D56',
                            light: '#E8F5EE',
                            bg: '#F5F7F5'
                        }
                    }
                }
            }
        }
    </script>

    <style>
        .no-scrollbar::-webkit-scrollbar { display: none; }
        .no-scrollbar { -ms-overflow-style: none; scrollbar-width: none; }
    </style>
</head>
<body class="bg-slate-200 min-h-screen flex justify-center items-center font-sans antialiased">

<!-- 앱 모바일 프레임 -->
<div id="app" class="w-full max-w-[390px] h-[844px] bg-[#F5F6F4] relative overflow-hidden flex flex-col shadow-2xl rounded-[40px] border border-slate-300">

    <!-- ---------------------------------------------------------------- border -->
    <!-- 1. 메인 화면 (시설에서 / 야외에서 / 집에서) -->
    <!-- ---------------------------------------------------------------- -->
    <div v-if="currentView === 'main'" class="flex-1 flex flex-col h-full overflow-hidden">
        <!-- 헤더 -->
        <div class="px-5 pt-7 pb-2 bg-[#F5F6F4]">
            <h1 class="text-2xl font-extrabold text-slate-900 tracking-tight">즉시 운동하기</h1>
            <p class="text-xs text-slate-400 mt-1 font-medium">지금 갈 수 있는 곳만 골라드려요 · 강서구 화곡동</p>
        </div>

        <!-- 메인 탭 (시설에서 / 야외에서 / 집에서) -->
        <div class="px-5 my-2">
            <div class="bg-[#EAECE9] p-1 rounded-2xl flex text-xs font-bold text-slate-500">
                <button @click="mainTab = 'facility'"
                        :class="mainTab === 'facility' ? 'bg-[#43996A] text-white shadow-sm' : 'text-slate-500 hover:text-slate-700'"
                        class="flex-1 py-2.5 rounded-xl transition-all text-center">
                    시설에서
                </button>
                <button @click="mainTab = 'outdoor'"
                        :class="mainTab === 'outdoor' ? 'bg-[#43996A] text-white shadow-sm' : 'text-slate-500 hover:text-slate-700'"
                        class="flex-1 py-2.5 rounded-xl transition-all text-center">
                    야외에서
                </button>
                <button @click="mainTab = 'home'"
                        :class="mainTab === 'home' ? 'bg-[#43996A] text-white shadow-sm' : 'text-slate-500 hover:text-slate-700'"
                        class="flex-1 py-2.5 rounded-xl transition-all text-center">
                    집에서
                </button>
            </div>
        </div>

        <!-- 콘텐츠 영역 -->
        <div class="flex-1 overflow-y-auto no-scrollbar px-5 pb-20 space-y-3">

            <!-- [탭 1] 시설에서 -->
            <div v-if="mainTab === 'facility'" class="space-y-3">
                <!-- 종목 서브 태그 -->
                <div class="flex space-x-1.5 overflow-x-auto no-scrollbar py-1">
                    <button v-for="cat in facilityCategories" :key="cat" @click="selectedFacilityCat = cat"
                            :class="selectedFacilityCat === cat ? 'bg-[#43996A] text-white' : 'bg-white text-slate-600 border border-slate-200/80'"
                            class="px-3.5 py-1.5 rounded-full text-xs font-semibold whitespace-nowrap shadow-xs">
                        {{ cat }}
                    </button>
                </div>

                <!-- 시설 카드 리스트 -->
                <div v-for="(facility, idx) in facilityList" :key="facility.id" @click="openFacilityDetail(facility)"
                     :class="idx === 0 ? 'border-2 border-[#43996A]' : 'border border-slate-200/80'"
                     class="bg-white p-4.5 rounded-2xl shadow-sm hover:border-[#43996A] cursor-pointer transition">
                    <div class="flex justify-between items-start">
                        <h3 class="font-bold text-slate-800 text-sm leading-snug">{{ facility.name }}</h3>
                        <span class="text-[11px] font-semibold text-[#43996A] bg-[#E8F5EE] px-2 py-0.5 rounded-full whitespace-nowrap ml-2">
                {{ facility.distance }}
              </span>
                    </div>
                    <p class="text-xs text-slate-400 mt-1 font-normal">{{ facility.address }}</p>
                </div>
            </div>

            <!-- [탭 2] 야외에서 -->
            <div v-if="mainTab === 'outdoor'" class="space-y-3">
                <!-- 야외 종목 서브 태그 -->
                <div class="flex space-x-1.5 py-1">
                    <button v-for="cat in ['걷기', '등산']" :key="cat" @click="selectedOutdoorCat = cat"
                            :class="selectedOutdoorCat === cat ? 'bg-[#43996A] text-white' : 'bg-white text-slate-600 border border-slate-200/80'"
                            class="px-4 py-1.5 rounded-full text-xs font-semibold">
                        {{ cat }}
                    </button>
                </div>

                <!-- 야외 코스 카드 리스트 -->
                <div v-for="(trail, idx) in outdoorList" :key="trail.id" @click="openOutdoorDetail(trail)"
                     :class="idx === 0 ? 'border-2 border-[#43996A]' : 'border border-slate-200/80'"
                     class="bg-white p-4.5 rounded-2xl shadow-sm cursor-pointer hover:border-[#43996A] transition">
                    <div class="flex justify-between items-start">
                        <h3 class="font-bold text-slate-800 text-sm leading-snug">{{ trail.title }}</h3>
                        <span class="text-[11px] font-semibold text-[#43996A] bg-[#E8F5EE] px-2 py-0.5 rounded-full whitespace-nowrap ml-2">
                {{ trail.distance }}
              </span>
                    </div>
                    <p class="text-xs text-slate-400 mt-1.5 font-normal">{{ trail.specs }}</p>
                    <p class="text-xs text-slate-400 mt-0.5 font-normal">{{ trail.routeType }}</p>
                </div>

                <!-- 지도 보기 버튼 -->
                <button class="w-full bg-[#43996A] text-white py-3.5 rounded-xl font-bold text-xs mt-2 shadow-sm hover:bg-[#357D56] transition">
                    지도 로 코스 10개 보기
                </button>
            </div>

            <!-- [탭 3] 집에서 -->
            <div v-if="mainTab === 'home'" class="space-y-4">
                <div class="space-y-3">
                    <!-- 강도 선택 -->
                    <div>
                        <label class="text-xs text-slate-400 font-medium block mb-1.5">강도</label>
                        <div class="grid grid-cols-3 gap-2">
                            <button v-for="lvl in ['가볍게', '적당히', '숨차게']" :key="lvl" @click="homeConfig.intensity = lvl"
                                    :class="homeConfig.intensity === lvl ? 'border-2 border-[#43996A] text-[#43996A] bg-[#E8F5EE] font-bold' : 'bg-white border border-slate-200 text-slate-600'"
                                    class="py-2.5 rounded-xl text-xs text-center transition">
                                {{ lvl }}
                            </button>
                        </div>
                    </div>

                    <!-- 소요시간 선택 -->
                    <div>
                        <label class="text-xs text-slate-400 font-medium block mb-1.5">소요시간</label>
                        <div class="grid grid-cols-3 gap-2">
                            <button v-for="time in ['10분', '20분', '30분']" :key="time" @click="homeConfig.duration = time"
                                    :class="homeConfig.duration === time ? 'border-2 border-[#43996A] text-[#43996A] bg-[#E8F5EE] font-bold' : 'bg-white border border-slate-200 text-slate-600'"
                                    class="py-2.5 rounded-xl text-xs text-center transition">
                                {{ time }}
                            </button>
                        </div>
                    </div>

                    <!-- 계획 만들기 버튼 -->
                    <button @click="generateHomePlan" class="w-full bg-[#43996A] text-white py-3.5 rounded-xl font-bold text-xs shadow-sm hover:bg-[#357D56] transition">
                        운동 계획 만들기
                    </button>
                </div>

                <!-- 생성된 계획 목록 -->
                <div v-if="homePlan" class="space-y-3 pt-2">
                    <!-- 준비운동 -->
                    <div>
                        <span class="text-[11px] font-bold text-[#43996A] block mb-1">준비운동 · 3분</span>
                        <div class="space-y-1.5">
                            <div v-for="item in homePlan.warmup" :key="item.name" class="bg-white px-3.5 py-2.5 rounded-xl border border-slate-200/70 flex justify-between text-xs">
                                <span class="font-bold text-slate-800">{{ item.name }}</span>
                                <span class="text-slate-400 font-medium">{{ item.reps }}</span>
                            </div>
                        </div>
                    </div>

                    <!-- 본운동 -->
                    <div>
                        <span class="text-[11px] font-bold text-[#43996A] block mb-1">본운동 · 14분</span>
                        <div class="space-y-1.5">
                            <div v-for="item in homePlan.main" :key="item.name" class="bg-white px-3.5 py-2.5 rounded-xl border border-slate-200/70 flex justify-between text-xs">
                                <span class="font-bold text-slate-800">{{ item.name }}</span>
                                <span class="text-slate-400 font-medium">{{ item.reps }}</span>
                            </div>
                        </div>
                    </div>

                    <!-- 마무리 -->
                    <div>
                        <span class="text-[11px] font-bold text-[#43996A] block mb-1">마무리 · 3분</span>
                        <div class="space-y-1.5">
                            <div v-for="item in homePlan.cooldown" :key="item.name" class="bg-white px-3.5 py-2.5 rounded-xl border border-slate-200/70 flex justify-between text-xs">
                                <span class="font-bold text-slate-800">{{ item.name }}</span>
                                <span class="text-slate-400 font-medium">{{ item.reps }}</span>
                            </div>
                        </div>
                    </div>

                    <!-- 하단 버튼 2개 -->
                    <div class="grid grid-cols-2 gap-2 pt-2">
                        <button @click="generateHomePlan" class="bg-white border border-slate-200 text-slate-700 py-3 rounded-xl font-bold text-xs hover:bg-slate-50">
                            다시 만들기
                        </button>
                        <button @click="startWorkout" class="bg-[#43996A] text-white py-3 rounded-xl font-bold text-xs shadow-sm hover:bg-[#357D56]">
                            운동 시작하기
                        </button>
                    </div>
                </div>
            </div>

        </div>
    </div>

    <!-- ---------------------------------------------------------------- border -->
    <!-- 2. 시설 상세 화면 (이미지 2) -->
    <!-- ---------------------------------------------------------------- -->
    <div v-if="currentView === 'facilityDetail'" class="flex-1 bg-[#F5F6F4] flex flex-col overflow-y-auto no-scrollbar pb-20">
        <div class="p-5 space-y-4">
            <!-- 뒤로가기 -->
            <button @click="currentView = 'main'" class="text-slate-700 text-lg"><i class="fa-solid fa-chevron-left"></i></button>

            <div>
                <h2 class="text-xl font-bold text-slate-900 leading-tight">{{ selectedFacility.name }}</h2>
                <p class="text-xs text-slate-400 mt-1 font-medium">수영 · {{ selectedFacility.address }} · {{ selectedFacility.distance }}</p>
            </div>

            <!-- 3개 요약 박스 -->
            <div class="grid grid-cols-3 gap-2">
                <div class="bg-white p-3 rounded-2xl border border-slate-200/80 text-center">
                    <span class="text-[11px] text-slate-400 block mb-0.5">이용요금</span>
                    <span class="text-xs font-extrabold text-slate-800">2,000원</span>
                </div>
                <div class="bg-white p-3 rounded-2xl border border-slate-200/80 text-center">
                    <span class="text-[11px] text-slate-400 block mb-0.5">평일</span>
                    <span class="text-xs font-extrabold text-slate-800">06:00~22:00</span>
                </div>
                <div class="bg-white p-3 rounded-2xl border border-slate-200/80 text-center">
                    <span class="text-[11px] text-slate-400 block mb-0.5">주말</span>
                    <span class="text-xs font-extrabold text-slate-800">09:00~18:00</span>
                </div>
            </div>

            <!-- 세부 정보 테이블 -->
            <div class="bg-white rounded-2xl p-4 border border-slate-200/80 space-y-2.5 text-xs">
                <div class="flex justify-between"><span class="text-slate-400">휴관일</span><span class="font-bold text-slate-800">매주 월요일, 1월 1일</span></div>
                <div class="border-t border-slate-100 pt-2.5 flex justify-between"><span class="text-slate-400">요금 기준</span><span class="font-bold text-slate-800">2시간 / 성인 1인</span></div>
                <div class="border-t border-slate-100 pt-2.5 flex justify-between"><span class="text-slate-400">수용 인원</span><span class="font-bold text-slate-800">60명</span></div>
                <div class="border-t border-slate-100 pt-2.5 flex justify-between"><span class="text-slate-400">문의</span><span class="font-bold text-slate-800">02-880-0000</span></div>
            </div>

            <!-- 경고 노란 박스 -->
            <div class="bg-[#FFF8E7] border border-[#FFE8B2] p-3.5 rounded-2xl text-center text-xs text-[#9E6B00] space-y-0.5">
                <p class="font-bold">요금·운영시간은 상시 변경될 수 있습니다</p>
                <p class="text-[11px] opacity-90">방문 전 안내 페이지에서 꼭 확인해 주세요</p>
            </div>

            <!-- 지도 영약 뷰 -->
            <div class="bg-slate-200 h-32 rounded-2xl relative overflow-hidden flex items-center justify-center border border-slate-300/60">
                <div class="absolute inset-0 opacity-20 bg-[radial-gradient(#000_1px,transparent_1px)] [background-size:12px_12px]"></div>
                <div class="w-6 h-6 bg-[#43996A] border-2 border-white rounded-full flex items-center justify-center text-white text-xs shadow-md z-10">
                    <div class="w-2 h-2 bg-white rounded-full"></div>
                </div>
            </div>
            <p class="text-xs text-slate-400 font-medium text-center">강서구 등촌동 707-3 · 9호선 등촌역 인근</p>

            <!-- 하단 액션 버튼 -->
            <div class="grid grid-cols-2 gap-2 pt-1">
                <button class="bg-white border border-slate-200 text-slate-800 py-3.5 rounded-xl font-bold text-xs hover:bg-slate-50">
                    길찾기
                </button>
                <button class="bg-[#43996A] text-white py-3.5 rounded-xl font-bold text-xs shadow-sm hover:bg-[#357D56]">
                    안내페이지로 이동
                </button>
            </div>
        </div>
    </div>

    <!-- ---------------------------------------------------------------- border -->
    <!-- 3. 야외 코스 상세 화면 (이미지 4) -->
    <!-- ---------------------------------------------------------------- -->
    <div v-if="currentView === 'outdoorDetail'" class="flex-1 bg-[#F5F6F4] flex flex-col overflow-y-auto no-scrollbar pb-20">
        <div class="p-5 space-y-4">
            <button @click="currentView = 'main'" class="text-slate-700 text-lg"><i class="fa-solid fa-chevron-left"></i></button>

            <div>
                <h2 class="text-xl font-bold text-slate-900 leading-tight">{{ selectedOutdoor.title }}</h2>
                <p class="text-xs text-slate-400 mt-1 font-medium">강서구 화곡동 · 시작점까지 0.8km · 순환형</p>
            </div>

            <!-- 트랙 지도 일러스트 -->
            <div class="bg-[#EFF4F0] h-40 rounded-2xl border border-slate-200/80 relative flex items-center justify-center p-4">
                <svg class="w-full h-full" viewBox="0 0 200 100">
                    <path d="M 30,50 C 30,20 170,20 170,50 C 170,80 30,80 30,50 Z" fill="none" stroke="#3A875A" stroke-width="4"/>
                    <circle cx="45" cy="65" r="4" fill="white" stroke="#3A875A" stroke-width="2"/>
                    <circle cx="120" cy="22" r="3" fill="#3A875A"/>
                </svg>
                <span class="absolute bottom-3 left-8 text-[10px] font-bold text-[#3A875A]">출발·도착</span>
                <span class="absolute top-3 right-12 text-[10px] font-bold text-[#3A875A]">봉수비 (117m)</span>
            </div>

            <!-- 3개 요약 박스 -->
            <div class="grid grid-cols-3 gap-2">
                <div class="bg-white p-3 rounded-2xl border border-slate-200/80 text-center">
                    <span class="text-[11px] text-slate-400 block mb-0.5">거리</span>
                    <span class="text-xs font-extrabold text-slate-800">3.2km</span>
                </div>
                <div class="bg-white p-3 rounded-2xl border border-slate-200/80 text-center">
                    <span class="text-[11px] text-slate-400 block mb-0.5">소요시간</span>
                    <span class="text-xs font-extrabold text-slate-800">약 60분</span>
                </div>
                <div class="bg-white p-3 rounded-2xl border border-slate-200/80 text-center">
                    <span class="text-[11px] text-slate-400 block mb-0.5">난이도</span>
                    <span class="text-xs font-extrabold text-slate-800">보통</span>
                </div>
            </div>

            <!-- 세부 정보 테이블 -->
            <div class="bg-white rounded-2xl p-4 border border-slate-200/80 space-y-2.5 text-xs">
                <div class="flex justify-between"><span class="text-slate-400">코스 특징</span><span class="font-bold text-slate-800">흙길 · 완만한 오르막</span></div>
                <div class="border-t border-slate-100 pt-2.5 flex justify-between"><span class="text-slate-400">연계 지하철</span><span class="font-bold text-slate-800">5호선 까치산역</span></div>
                <div class="border-t border-slate-100 pt-2.5 flex justify-between"><span class="text-slate-400">예상 소모 칼로리</span><span class="font-bold text-slate-800">약 240kcal</span></div>
                <div class="border-t border-slate-100 pt-2.5 flex justify-between"><span class="text-slate-400">주요 지점</span><span class="font-bold text-slate-800">그리스도대 · 오리나무 쉼터</span></div>
                <div class="border-t border-slate-100 pt-2.5 flex justify-between"><span class="text-slate-400">출처</span><span class="font-bold text-slate-800">강서둘레길 · 두루누비</span></div>
            </div>

            <!-- 연한 녹색 안내 박스 -->
            <div class="bg-[#E8F5EE] border border-[#C6E6D4] p-3 rounded-2xl text-center text-xs text-[#2E6B48] space-y-0.5">
                <p class="font-bold">지도는 대략적인 경로입니다</p>
                <p class="text-[11px] opacity-90">실제 길찾기는 카카오맵에서 확인해주세요</p>
            </div>

            <!-- 하단 액션 버튼 -->
            <div class="grid grid-cols-2 gap-2 pt-1">
                <button class="bg-white border border-slate-200 text-slate-800 py-3.5 rounded-xl font-bold text-xs hover:bg-slate-50">
                    길찾기
                </button>
                <button class="bg-[#43996A] text-white py-3.5 rounded-xl font-bold text-xs shadow-sm hover:bg-[#357D56]">
                    안내페이지로 이동
                </button>
            </div>
        </div>
    </div>

    <!-- ---------------------------------------------------------------- border -->
    <!-- 4. 운동 진행 화면 (이미지 6) -->
    <!-- ---------------------------------------------------------------- -->
    <div v-if="currentView === 'workoutExecution'" class="flex-1 bg-[#F5F6F4] flex flex-col justify-between p-5 pb-20">
        <div>
            <!-- 상단 헤더 -->
            <div class="flex justify-between items-center text-xs font-bold text-slate-400 mb-3">
                <button @click="currentView = 'main'" class="text-slate-700 text-base"><i class="fa-solid fa-chevron-left"></i></button>
                <span>동작 {{ currentStepIndex + 1 }} / {{ activePlan.exercises.length }} · 남은 14분</span>
                <button class="text-[#43996A]">일시정지</button>
            </div>

            <!-- 프로그레스 세그먼트 Bar -->
            <div class="grid grid-cols-6 gap-1 mb-4">
                <div v-for="(ex, i) in activePlan.exercises" :key="i"
                     :class="i <= currentStepIndex ? 'bg-[#43996A]' : 'bg-slate-200'"
                     class="h-1.5 rounded-full transition-all"></div>
            </div>

            <!-- 운동 세트 뱃지 -->
            <div class="inline-block bg-[#1B4B32] text-white text-[11px] font-bold px-3 py-1 rounded-full mb-3">
                {{ currentSet }}세트 / {{ currentExercise.sets }}세트
            </div>

            <!-- 운동 이미지 가상 영역 -->
            <div class="bg-slate-200/70 border border-slate-300/50 rounded-2xl h-44 flex items-center justify-center text-xs font-bold text-slate-400 mb-4">
                {{ currentExercise.name }} 동작 이미지
            </div>

            <!-- 운동 이름 및 설명 -->
            <div class="flex justify-between items-start mb-3">
                <div>
                    <h2 class="text-xl font-extrabold text-slate-900">{{ currentExercise.name }}</h2>
                    <p class="text-xs text-slate-400 mt-1 max-w-[200px] leading-relaxed font-normal">{{ currentExercise.tip }}</p>
                </div>
                <div class="text-right">
                    <span class="text-[10px] text-slate-400 block font-medium">이번 세트</span>
                    <span class="text-xl font-extrabold text-[#43996A]">{{ currentExercise.reps }}</span>
                    <span class="text-[10px] text-slate-400 block mt-0.5">휴식 30초 자동</span>
                </div>
            </div>

            <!-- 세트 체크 상태 -->
            <div class="flex space-x-2 mb-4">
                <div v-for="s in currentExercise.sets" :key="s"
                     :class="s < currentSet ? 'bg-[#43996A] text-white' : (s === currentSet ? 'border-2 border-[#43996A] text-[#43996A] bg-white' : 'bg-slate-200 text-slate-400')"
                     class="w-7 h-7 rounded-lg flex items-center justify-center text-xs font-bold">
                    <i v-if="s < currentSet" class="fa-solid fa-check text-xs"></i>
                    <span v-else>{{ s }}</span>
                </div>
            </div>

            <!-- 다음 동작 미리보기 박스 -->
            <div class="bg-white border border-slate-200/80 rounded-2xl p-3.5 flex justify-between items-center text-xs">
                <div>
                    <span class="text-[10px] text-slate-400 block font-medium">다음동작</span>
                    <span class="font-bold text-slate-800">플랭크 · 30초 x 3세트</span>
                </div>
                <i class="fa-solid fa-chevron-right text-slate-300 text-xs"></i>
            </div>
        </div>

        <!-- 하단 진행 버튼 -->
        <div class="grid grid-cols-2 gap-2 mt-4">
            <button @click="skipStep" class="bg-white border border-slate-200 text-slate-700 py-3.5 rounded-xl font-bold text-xs hover:bg-slate-50">
                건너뛰기
            </button>
            <button @click="completeSet" class="bg-[#43996A] text-white py-3.5 rounded-xl font-bold text-xs shadow-sm hover:bg-[#357D56]">
                세트 완료
            </button>
        </div>
    </div>

    <!-- ---------------------------------------------------------------- border -->
    <!-- 5. 운동 완료 화면 (이미지 7) -->
    <!-- ---------------------------------------------------------------- -->
    <div v-if="currentView === 'workoutDone'" class="flex-1 bg-[#F5F6F4] flex flex-col justify-between p-5 pb-20 overflow-y-auto no-scrollbar">
        <div class="space-y-4">
            <p class="text-xs text-center text-slate-400 font-bold pt-2">8월 5일 (수)</p>

            <!-- 완료 큰 초록 카드 -->
            <div class="bg-[#43996A] text-white p-6 rounded-3xl text-center shadow-sm space-y-1">
                <p class="text-xs font-medium opacity-90">오늘의 운동 완료</p>
                <h2 class="text-2xl font-extrabold tracking-tight">20분 · 96kcal</h2>
            </div>

            <!-- 3개 요약 정보 -->
            <div class="grid grid-cols-3 gap-2">
                <div class="bg-white p-3 rounded-2xl border border-slate-200/80 text-center">
                    <span class="text-[10px] text-slate-400 block mb-0.5">완료동작</span>
                    <span class="text-xs font-extrabold text-slate-800">6/6</span>
                </div>
                <div class="bg-white p-3 rounded-2xl border border-slate-200/80 text-center">
                    <span class="text-[10px] text-slate-400 block mb-0.5">총세트</span>
                    <span class="text-xs font-extrabold text-slate-800">16세트</span>
                </div>
                <div class="bg-white p-3 rounded-2xl border border-slate-200/80 text-center">
                    <span class="text-[10px] text-slate-400 block mb-0.5">연속출석</span>
                    <span class="text-xs font-extrabold text-slate-800">4일째</span>
                </div>
            </div>

            <!-- 수행 목록 -->
            <div class="space-y-1.5">
                <div v-for="ex in completedExercises" :key="ex.name"
                     class="bg-white px-4 py-2.5 rounded-xl border border-slate-200/70 flex justify-between text-xs">
                    <span class="font-bold text-slate-800">{{ ex.name }}</span>
                    <span class="text-slate-400 font-medium">{{ ex.reps }}</span>
                </div>
            </div>
        </div>

        <!-- 하단 네비게이션 버튼 -->
        <div class="grid grid-cols-2 gap-2 pt-4">
            <button @click="currentView = 'main'" class="bg-white border border-slate-200 text-slate-700 py-3.5 rounded-xl font-bold text-xs hover:bg-slate-50">
                홈으로
            </button>
            <button @click="currentView = 'main'" class="bg-[#43996A] text-white py-3.5 rounded-xl font-bold text-xs shadow-sm hover:bg-[#357D56]">
                내 리포트 보기
            </button>
        </div>
    </div>

    <!-- ---------------------------------------------------------------- border -->
    <!-- 하단 메인 네비게이션 바 (모든 주요 화면에서 고정) -->
    <!-- ---------------------------------------------------------------- -->
    <div class="absolute bottom-0 left-0 right-0 h-16 bg-white border-t border-slate-200/70 flex justify-around items-center text-xs z-30 px-2">
        <button class="flex flex-col items-center space-y-1 text-slate-400 hover:text-slate-600">
            <i class="fa-bullseye fa-solid text-lg"></i>
            <span class="text-[10px] font-medium">추천</span>
        </button>
        <button @click="currentView = 'main'" class="flex flex-col items-center space-y-1 text-[#43996A]">
            <i class="fa-solid fa-person-running text-xl"></i>
            <span class="text-[10px] font-bold">즉시운동</span>
        </button>
        <button class="flex flex-col items-center space-y-1 text-slate-400 hover:text-slate-600">
            <i class="fa-regular fa-calendar-days text-lg"></i>
            <span class="text-[10px] font-medium">행사</span>
        </button>
        <button class="flex flex-col items-center space-y-1 text-slate-400 hover:text-slate-600">
            <i class="fa-regular fa-user text-lg"></i>
            <span class="text-[10px] font-medium">내정보</span>
        </button>
    </div>

</div>

<!-- Vue 3 스크립트 로직 -->
<script>
    const { createApp } = Vue;

    createApp({
        data() {
            return {
                currentView: 'main', // 'main', 'facilityDetail', 'outdoorDetail', 'workoutExecution', 'workoutDone'
                mainTab: 'facility', // 'facility', 'outdoor', 'home'

                // [탭 1] 시설 데이터
                facilityCategories: ['수영', '배드민턴', '헬스', '탁구', '테니스', '농구', '골프'],
                selectedFacilityCat: '수영',
                selectedFacility: {},
                facilityList: [
                    { id: 1, name: '강서구민올림픽체육센터 수영장', address: '강서구 등촌동 707-3', distance: '2.2km' },
                    { id: 2, name: '공항동문화체육센터 수영장', address: '강서구 송정로 45', distance: '3.3km' },
                    { id: 3, name: '목동청소년수련관 수영장', address: '양천구 목동서로 143', distance: '3.3km' },
                    { id: 4, name: '마곡레포츠센터 수영장', address: '강서구 양천로 251', distance: '3.6km' }
                ],

                // [탭 2] 야외 코스 데이터
                selectedOutdoorCat: '걷기',
                selectedOutdoor: {},
                outdoorList: [
                    { id: 1, title: '봉제산 둘레길', distance: '0.8km', specs: '3.2km · 약 60분 · 보통', routeType: '순환형 · 5호선 까치산역' },
                    { id: 2, title: '우장산 근린공원 산책로', distance: '1.0km', specs: '1.8km · 약 30분 · 쉬움', routeType: '무장애 구간 · 5호선 우장산역' },
                    { id: 3, title: '강서둘레길 3코스 · 강서한강길', distance: '4.8km', specs: '6.5km · 약 100분 · 쉬움', routeType: '평지 · 9호선 양천향교역' },
                    { id: 4, title: '강서둘레길 1코스 · 개화산숲길', distance: '5.4km', specs: '4.0km · 약 70분 · 보통', routeType: '순환형 · 5호선 개화산역' }
                ],

                // [탭 3] 집에서 데이터
                homeConfig: { intensity: '적당히', duration: '20분' },
                homePlan: {
                    warmup: [
                        { name: '목 돌리기', reps: '10회 x 2세트' },
                        { name: '어깨 스트레칭', reps: '30초 x 2세트' }
                    ],
                    main: [
                        { name: '윗몸일으키기', reps: '10회 x 3세트' },
                        { name: '스쿼트', reps: '15회 x 3세트' },
                        { name: '플랭크', reps: '30초 x 2세트' }
                    ],
                    cooldown: [
                        { name: '햄스트링 스트레칭', reps: '30초 x 2세트' }
                    ]
                },

                // 운동 진행 상태
                activePlan: {
                    exercises: [
                        { name: '목 돌리기', reps: '10회', sets: 2, tip: '목에 무리가 가지 않도록 천천히 돌려주세요.' },
                        { name: '어깨 스트레칭', reps: '30초', sets: 2, tip: '숨을 내쉬며 팔을 당겨주세요.' },
                        { name: '윗몸일으키기', reps: '10회', sets: 3, tip: '반동을 이용하지 않고 코어 힘에 집중합니다.' },
                        { name: '스쿼트', reps: '15회', sets: 3, tip: '무릎이 발끝을 넘지 않게, 천천히 앉았다가 일어나기' },
                        { name: '플랭크', reps: '30초', sets: 2, tip: '복부와 엉덩이에 힘을 유지하세요.' },
                        { name: '햄스트링 스트레칭', reps: '30초', sets: 2, tip: '허벅지 뒷근육이 늘어남을 느껴보세요.' }
                    ]
                },
                currentStepIndex: 3, // 스쿼트 위치
                currentSet: 2,

                completedExercises: [
                    { name: '목 돌리기', reps: '10회 x 2세트' },
                    { name: '어깨 스트레칭', reps: '30초 x 2세트' },
                    { name: '윗몸일으키기', reps: '10회 x 3세트' },
                    { name: '스쿼트', reps: '15회 x 3세트' },
                    { name: '플랭크', reps: '30초 x 2세트' },
                    { name: '햄스트링 스트레칭', reps: '30초 x 2세트' }
                ]
            }
        },
        computed: {
            currentExercise() {
                return this.activePlan.exercises[this.currentStepIndex] || {};
            }
        },
        methods: {
            openFacilityDetail(facility) {
                this.selectedFacility = facility;
                this.currentView = 'facilityDetail';
            },
            openOutdoorDetail(trail) {
                this.selectedOutdoor = trail;
                this.currentView = 'outdoorDetail';
            },
            generateHomePlan() {
                this.homePlan = {
                    warmup: [
                        { name: '목 돌리기', reps: '10회 x 2세트' },
                        { name: '어깨 스트레칭', reps: '30초 x 2세트' }
                    ],
                    main: [
                        { name: '윗몸일으키기', reps: '10회 x 3세트' },
                        { name: '스쿼트', reps: '15회 x 3세트' },
                        { name: '플랭크', reps: '30초 x 2세트' }
                    ],
                    cooldown: [
                        { name: '햄스트링 스트레칭', reps: '30초 x 2세트' }
                    ]
                };
            },
            startWorkout() {
                this.currentStepIndex = 3;
                this.currentSet = 2;
                this.currentView = 'workoutExecution';
            },
            completeSet() {
                if (this.currentSet < this.currentExercise.sets) {
                    this.currentSet++;
                } else {
                    this.skipStep();
                }
            },
            skipStep() {
                if (this.currentStepIndex < this.activePlan.exercises.length - 1) {
                    this.currentStepIndex++;
                    this.currentSet = 1;
                } else {
                    this.currentView = 'workoutDone';
                }
            }
        }
    }).mount('#app');
</script>
</body>
</html>
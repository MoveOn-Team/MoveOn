(function () {
    "use strict";

    document.addEventListener("DOMContentLoaded", function () {
        var body = document.body;
        var contextPath = body.dataset.contextPath || "";

        appendPositionToLinks(body);
        bindMoreButton();
        bindMaps();
        bindHomePlan(contextPath);
        bindHomePlay(contextPath);
    });

    function appendPositionToLinks(body) {
        var lat = body.dataset.lat;
        var lng = body.dataset.lng;
        if (!lat || !lng) {
            return;
        }
        document.querySelectorAll(".main-tab-btn, .sub-tag-btn").forEach(function (a) {
            if (!a.href || a.href.indexOf("lat=") >= 0) {
                return;
            }
            a.href += (a.href.indexOf("?") < 0 ? "?" : "&") + "lat=" + lat + "&lng=" + lng;
        });
    }

    function bindMoreButton() {
        var btnMore = document.querySelector("[data-more]");
        if (!btnMore) {
            return;
        }
        btnMore.addEventListener("click", function () {
            document.querySelectorAll(".facility-card.is-folded").forEach(function (card) {
                card.classList.remove("is-folded");
            });
            btnMore.remove();
        });
    }

    function bindHomePlan(contextPath) {
        var picker = document.getElementById("homeWorkoutPicker");
        var result = document.getElementById("workoutPlanResult");
        if (!picker || !result) {
            return;
        }

        var state = {
            intensity: "MODERATE",
            targetMin: "20"
        };

        picker.querySelectorAll("[data-home-options]").forEach(function (group) {
            var key = group.dataset.homeOptions;
            group.querySelectorAll(".option-btn").forEach(function (btn) {
                if (btn.classList.contains("is-active")) {
                    state[key] = btn.dataset.value;
                }
                btn.addEventListener("click", function () {
                    group.querySelectorAll(".option-btn").forEach(function (item) {
                        item.classList.remove("is-active");
                    });
                    btn.classList.add("is-active");
                    state[key] = btn.dataset.value;
                    hidePlan(result);
                });
            });
        });

        document.getElementById("btnMakeHomePlan").addEventListener("click", function () {
            var params = new URLSearchParams();
            params.set("intensity", state.intensity);
            params.set("targetMin", state.targetMin);

            result.classList.remove("is-hidden");
            result.innerHTML = '<div class="empty-box">운동 계획을 만들고 있어요.</div>';

            fetch(contextPath + "/workout/api/home-plan", {
                method: "POST",
                headers: {"Content-Type": "application/x-www-form-urlencoded;charset=UTF-8"},
                body: params.toString()
            })
                .then(function (res) {
                    if (!res.ok) {
                        throw new Error("plan request failed");
                    }
                    return res.json();
                })
                .then(function (plan) {
                    renderPlan(result, plan, contextPath);
                })
                .catch(function () {
                    result.innerHTML = '<div class="empty-box">운동 계획을 만들지 못했어요.<br>잠시 후 다시 시도해 주세요.</div>';
                });
        });
    }

    function renderPlan(result, plan, contextPath) {
        if (!plan || !plan.exercises || plan.exercises.length === 0) {
            result.innerHTML = '<div class="empty-box">조건에 맞는 홈트 운동이 아직 없습니다.<br>home_workout_rules 데이터를 확인해 주세요.</div>';
            return;
        }

        result.innerHTML =
            renderGroup("준비운동", plan.warmupMin, plan.warmups) +
            renderGroup("본운동", plan.mainMin, plan.mains) +
            renderGroup("마무리", plan.cooldownMin, plan.cooldowns) +
            '<div class="home-action-group">' +
            '<button type="button" class="btn-outline" id="btnResetHomePlan">다시 만들기</button>' +
            '<a href="' + contextPath + '/workout/workoutPlay" class="btn-main-action">운동 시작하기</a>' +
            '</div>';

        document.getElementById("btnResetHomePlan").addEventListener("click", function () {
            document.querySelectorAll("#homeWorkoutPicker .option-grid").forEach(function (group) {
                group.querySelectorAll(".option-btn").forEach(function (btn, idx) {
                    btn.classList.toggle("is-active", idx === 1);
                });
            });
            hidePlan(result);
        });
    }

    function renderGroup(title, minutes, items) {
        if (!items || items.length === 0) {
            return "";
        }
        return '<div class="routine-group">' +
            '<strong class="routine-title">' + title + ' · ' + minutes + '분</strong>' +
            items.map(function (item) {
                return '<div class="routine-card">' +
                    '<span>' + esc(item.name) + '</span>' +
                    '<span class="count-badge">' + volume(item) + '</span>' +
                    '</div>';
            }).join("") +
            '</div>';
    }

    function hidePlan(result) {
        result.classList.add("is-hidden");
        result.innerHTML = "";
    }

    function bindHomePlay(contextPath) {
        var play = document.getElementById("homeWorkoutPlay");
        if (!play) {
            return;
        }

        fetch(contextPath + "/workout/api/home-plan")
            .then(function (res) { return res.json(); })
            .then(function (plan) {
                if (!plan || !plan.exercises || plan.exercises.length === 0) {
                    location.href = contextPath + "/workout/workoutList?tab=home";
                    return;
                }
                runWorkout(contextPath, plan);
            });
    }

    function runWorkout(contextPath, plan) {
        var exercises = plan.exercises;
        var exerciseIndex = 0;
        var setIndex = 0;
        var setState = exercises.map(function (exercise) {
            return new Array(exercise.sets).fill("pending");
        });

        var statusText = document.getElementById("statusText");
        var progressBarGroup = document.getElementById("progressBarGroup");
        var setTagBadge = document.getElementById("setTagBadge");
        var mediaPlaceholder = document.getElementById("mediaPlaceholder");
        var exerciseTitle = document.getElementById("exerciseTitle");
        var exerciseDesc = document.getElementById("exerciseDesc");
        var setCheckGroup = document.getElementById("setCheckGroup");
        var setValue = document.getElementById("setValue");
        var restLabel = document.getElementById("restLabel");
        var nextExerciseName = document.getElementById("nextExerciseName");

        progressBarGroup.innerHTML = exercises.map(function () {
            return '<div class="progress-step"></div>';
        }).join("");

        document.getElementById("btnComplete").addEventListener("click", function () {
            setState[exerciseIndex][setIndex] = "done";
            advance();
        });

        document.getElementById("btnSkip").addEventListener("click", function () {
            setState[exerciseIndex][setIndex] = "skipped";
            advance();
        });

        render();

        function advance() {
            if (setIndex + 1 < exercises[exerciseIndex].sets) {
                setIndex++;
                render();
                return;
            }
            if (exerciseIndex + 1 < exercises.length) {
                exerciseIndex++;
                setIndex = 0;
                render();
                return;
            }
            finish();
        }

        function render() {
            var exercise = exercises[exerciseIndex];
            var remaining = exercises.slice(exerciseIndex)
                .reduce(function (sum, item) { return sum + (item.estimatedMin || 0); }, 0);

            statusText.innerText = "동작 " + (exerciseIndex + 1) + "/" + exercises.length + " · 남은 " + Math.max(1, remaining) + "분";
            setTagBadge.innerText = (setIndex + 1) + "세트 / " + exercise.sets + "세트";
            exerciseTitle.innerText = exercise.name;
            exerciseDesc.innerText = exercise.instruction || "천천히 정확한 자세로 진행하세요.";
            setValue.innerText = exercise.reps ? exercise.reps + "회" : exercise.durationSec + "초";
            restLabel.innerText = "휴식 " + exercise.restSec + "초 자동";
            nextExerciseName.innerText = exercises[exerciseIndex + 1]
                ? exercises[exerciseIndex + 1].name + " · " + volume(exercises[exerciseIndex + 1])
                : "마지막 동작";

            if (exercise.mediaUrl) {
                mediaPlaceholder.innerHTML = '<img class="exercise-media-img" src="' + esc(exercise.mediaUrl) + '" alt="' + esc(exercise.name) + '">';
            } else {
                mediaPlaceholder.innerText = exercise.name + " 동작 이미지";
            }

            progressBarGroup.querySelectorAll(".progress-step").forEach(function (step, idx) {
                step.classList.toggle("is-active", idx <= exerciseIndex);
            });

            setCheckGroup.innerHTML = setState[exerciseIndex].map(function (state, idx) {
                var cls = "set-check-item";
                var text = idx + 1;
                if (state === "done") {
                    cls += " is-done";
                    text = "✓";
                } else if (state === "skipped") {
                    cls += " is-skipped";
                    text = "-";
                } else if (idx === setIndex) {
                    cls += " is-current";
                }
                return '<span class="' + cls + '">' + text + '</span>';
            }).join("");
        }

        /*
        수정 부분
         */

        function finish() {
            var totalCompletedSetCount = 0;
            var totalSkippedSetCount = 0;
            var totalBurnedCalories = 0;

            var exerciseRecords = exercises.map(function (ex, idx) {
                var completedSets = setState[idx].filter(function (s) { return s === "done"; }).length;
                var skippedSets = setState[idx].filter(function (s) { return s === "skipped"; }).length;

                totalCompletedSetCount += completedSets;
                totalSkippedSetCount += skippedSets;

                // [수정된 표준 칼로리 공식]
                var met = ex.metValue || 4.5;               // 기본 MET값 4.5 (중강도 운동)
                var durationSec = ex.durationSec || 30;      // 세트당 수행시간(초)
                var userWeight = parseFloat(document.body.dataset.userWeight) || 65;      // 사용자 기본 체중(kg)

                // (MET * 0.0175 * 체중 * 운동시간(분)) * 완료 세트 수
                var durationMin = durationSec / 60.0;
                var setCalorie = met * 0.0175 * userWeight * durationMin;

                totalBurnedCalories += (setCalorie * completedSets);

                return {
                    exerciseId: ex.exerciseId || ex.id,
                    totalSets: ex.sets,
                    completedSets: completedSets,
                    skippedSets: skippedSets,
                    durationSec: ex.durationSec || 0,
                    reps: ex.reps || 0
                };
            });

            // 운동 전체 휴식시간 및 유효 강도를 고려한 기본 베이스 칼로리 보정 (최소 분당 4kcal 보장)
            var finalCalories = Math.max(
                Math.round(totalBurnedCalories),
                Math.round(totalCompletedSetCount * 4.5) // 세트당 최소 약 4.5kcal 집계
            );

            fetch(contextPath + "/workout/api/home-result", {
                method: "POST",
                headers: {"Content-Type": "application/json;charset=UTF-8"},
                body: JSON.stringify({
                    completedExerciseCount: exercises.length,
                    completedSetCount: totalCompletedSetCount,
                    skippedSetCount: totalSkippedSetCount,
                    burnedCalories: finalCalories, // 보정된 칼로리 전달
                    exerciseRecords: exerciseRecords
                })
            })
                .then(function (res) { return res.json(); })
                .then(function (res) {
                    location.href = contextPath + (res.redirectUrl || "/workout/workoutResult");
                });
        }
    }

    function volume(item) {
        var unit = item.reps ? item.reps + "회" : item.durationSec + "초";
        return unit + " x " + item.sets + "세트";
    }

    function esc(value) {
        return String(value == null ? "" : value)
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#39;");
    }

    function bindMaps() {
        var mapBox = document.getElementById("courseMap");
        if (mapBox && window.kakao && kakao.maps) {
            drawCourseMap(mapBox);
        }

        var facBox = document.getElementById("facilityMap");
        if (facBox && window.kakao && kakao.maps) {
            var at = new kakao.maps.LatLng(
                parseFloat(facBox.dataset.lat), parseFloat(facBox.dataset.lng));
            var facMap = new kakao.maps.Map(facBox, {center: at, level: 4});
            addMark(facMap, at, facBox.dataset.name || "");
            setTimeout(function () {
                facMap.relayout();
                facMap.setCenter(at);
            }, 200);
        }

        openFrom(document.getElementById("btnCourseRoute"), "mapUrl",
            "이 코스의 위치 정보가 아직 없습니다.");
        openFrom(document.getElementById("btnFacilityMap"), "mapUrl",
            "이 시설의 위치 정보가 아직 없습니다.");
        openFrom(document.getElementById("btnFacilitySite"), "targetUrl",
            "이 시설의 안내 페이지가 등록되어 있지 않습니다.");
    }

    function drawCourseMap(box) {
        var pts;
        try {
            pts = JSON.parse(box.dataset.points || "[]");
        } catch (e) {
            return;
        }
        if (pts.length < 2) {
            return;
        }

        var path = pts.map(function (p) {
            return new kakao.maps.LatLng(p[0], p[1]);
        });
        var map = new kakao.maps.Map(box, {center: path[0], level: 5});
        var bounds = new kakao.maps.LatLngBounds();
        path.forEach(function (ll) { bounds.extend(ll); });
        map.setBounds(bounds, 40, 40, 40, 40);

        if (box.dataset.line !== "false") {
            var line = path.slice();
            if (box.dataset.loop === "true") {
                line.push(line[0]);
            }
            new kakao.maps.Polyline({
                map: map,
                path: line,
                strokeWeight: 5,
                strokeColor: "#4a9a72",
                strokeOpacity: 0.9
            });
        }

        addMark(map, path[0], box.dataset.loop === "true" ? "출발·도착" : "출발");
        if (box.dataset.loop !== "true") {
            addMark(map, path[path.length - 1], "도착");
        }

        setTimeout(function () {
            map.relayout();
            map.setBounds(bounds, 40, 40, 40, 40);
        }, 200);
    }

    function addMark(map, latlng, text) {
        new kakao.maps.Marker({map: map, position: latlng});
        new kakao.maps.CustomOverlay({
            map: map,
            position: latlng,
            yAnchor: 2.2,
            content: '<span class="map-pin-label">' + esc(text) + '</span>'
        });
    }

    function openFrom(el, attr, emptyMsg) {
        if (!el) {
            return;
        }
        el.addEventListener("click", function () {
            var url = el.dataset[attr];
            if (url) {
                window.open(url, "_blank", "noopener");
            } else {
                alert(emptyMsg);
            }
        });
    }
})();

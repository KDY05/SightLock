# SightLock

- Requirements: Paper 1.21.8+ / Java 21+

몹 또는 플레이어를 시선에 고정시켜 쉽게 이동시킬 수 있는 플러그인입니다.

## Features
- 전용 도구(기본값: 네더라이트 괭이)를 이용하여 대상 시선에 고정하고 조작할 수 있습니다.
- 전용 도구 사용 방법
  - 우클릭 : 고정 / 고정 해제
  - F : 밀기
  - Shift + F : 당기기

## Commands
- ```/sl``` - 기능을 토글합니다. 서버 시작 혹은 리로드 시에 기본적으로 비활성화됩니다.
- ```/sl help``` - 도움말을 표시합니다.
- ```/sl reload``` - 설정 파일을 다시 불러옵니다.
- ```/sl status``` - 플러그인 관련 상태를 확인합니다.

## Permissions
- ```sightlock.use``` - 명령어 사용 권한 (기본값: OP)
- ```sightlock.reload``` - reload 명령어 사용 권한 (기본값: OP)

## Configuration
- config.yml에서 전용 도구를 변경할 수 있습니다.
- lang.yml에서 플러그인 메시지를 변경할 수 있습니다.

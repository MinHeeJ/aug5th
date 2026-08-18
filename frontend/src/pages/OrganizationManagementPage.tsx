import { FormEvent, useEffect, useMemo, useState } from "react";
import {
  getOrganizationTree,
  listOrganizations,
  Organization,
  OrganizationTreeNode,
  saveOrganizationRelationship,
} from "../api/admin";

const organizationTypes = [
  "UNIVERSITY",
  "GRADUATE_SCHOOL",
  "COLLEGE",
  "DEPARTMENT",
  "ADMIN_DEPARTMENT",
];

function flattenTree(nodes: OrganizationTreeNode[]): OrganizationTreeNode[] {
  return nodes.flatMap((node) => [node, ...flattenTree(node.children ?? [])]);
}

function TreeList({
  nodes,
  onSelect,
}: {
  nodes: OrganizationTreeNode[];
  onSelect: (node: OrganizationTreeNode) => void;
}) {
  return (
    <ul className="space-y-2">
      {nodes.map((node) => (
        <li key={node.organizationId}>
          <button
            type="button"
            onClick={() => onSelect(node)}
            className="rounded-lg border px-3 py-1 text-left"
          >
            {node.organizationCode} {node.organizationName}
          </button>
          {node.children.length > 0 && (
            <div className="ml-4 mt-2">
              <TreeList nodes={node.children} onSelect={onSelect} />
            </div>
          )}
        </li>
      ))}
    </ul>
  );
}

export default function OrganizationManagementPage() {
  const [organizationCode, setOrganizationCode] = useState("");
  const [organizationType, setOrganizationType] = useState("");
  const [organizations, setOrganizations] = useState<Organization[]>([]);
  const [tree, setTree] = useState<OrganizationTreeNode[]>([]);
  const [selectedId, setSelectedId] = useState("");
  const [parentId, setParentId] = useState<string>("");
  const [effectiveStartDate, setEffectiveStartDate] = useState("");
  const [effectiveEndDate, setEffectiveEndDate] = useState("");
  const [changeReason, setChangeReason] = useState("");
  const [status, setStatus] = useState<
    "loading" | "empty" | "error" | "permission" | "success"
  >("loading");
  const [message, setMessage] = useState("");

  const flatTree = useMemo(() => flattenTree(tree), [tree]);
  const selected = useMemo(
    () =>
      organizations.find((org) => org.organizationId === selectedId) ??
      flatTree.find((org) => org.organizationId === selectedId),
    [flatTree, organizations, selectedId],
  );

  async function loadOrganizations() {
    setStatus("loading");
    try {
      const data = await listOrganizations({
        organizationCode,
        organizationType,
      });
      setOrganizations(data);
      setStatus(data.length === 0 ? "empty" : "success");
      setMessage(
        data.length === 0
          ? "조건에 맞는 조직 없음"
          : "조직 목록을 조회했습니다.",
      );
    } catch (error) {
      const apiError = error as {
        status?: number;
        payload?: { error?: { message?: string } };
      };
      setStatus(
        apiError.status === 401 || apiError.status === 403
          ? "permission"
          : "error",
      );
      setMessage(
        apiError.payload?.error?.message ?? "조직 목록을 조회할 수 없습니다.",
      );
    }
  }

  async function refreshTree() {
    setStatus("loading");
    try {
      const data = await getOrganizationTree();
      setTree(data);
      setStatus(data.length === 0 ? "empty" : "success");
      setMessage(
        data.length === 0
          ? "등록된 상하위 관계 없음"
          : "조직 계층을 조회했습니다.",
      );
    } catch (error) {
      const apiError = error as {
        status?: number;
        payload?: { error?: { message?: string } };
      };
      setStatus(
        apiError.status === 401 || apiError.status === 403
          ? "permission"
          : "error",
      );
      setMessage(
        apiError.payload?.error?.message ?? "조직 계층을 조회할 수 없습니다.",
      );
    }
  }

  useEffect(() => {
    void loadOrganizations();
    void refreshTree();
  }, []);

  function selectOrganization(org: Organization | OrganizationTreeNode) {
    setSelectedId(org.organizationId);
    const treeNode = flatTree.find(
      (node) => node.organizationId === org.organizationId,
    );
    setParentId(treeNode?.parentOrganizationId ?? "");
    setEffectiveStartDate(
      treeNode?.effectiveStartDate ?? new Date().toISOString().slice(0, 10),
    );
    setEffectiveEndDate(treeNode?.effectiveEndDate ?? "");
    setChangeReason("");
  }

  function submitSearch(event: FormEvent) {
    event.preventDefault();
    void loadOrganizations();
  }

  async function saveRelationship() {
    if (!selected) {
      setMessage("조직을 선택하세요");
      return;
    }
    if (effectiveEndDate && effectiveStartDate > effectiveEndDate) {
      setStatus("error");
      setMessage("적용 종료일은 적용 시작일보다 빠를 수 없습니다.");
      return;
    }
    const existing = flatTree.find(
      (node) => node.organizationId === selected.organizationId,
    );
    const relationshipId = existing?.relationshipId ?? crypto.randomUUID();
    try {
      await saveOrganizationRelationship(relationshipId, {
        organizationId: selected.organizationId,
        parentOrganizationId: parentId || null,
        effectiveStartDate,
        effectiveEndDate: effectiveEndDate || null,
        changeReason,
      });
      setMessage("조직 관계와 적용기간이 저장되었습니다");
      await refreshTree();
    } catch (error) {
      const apiError = error as {
        status?: number;
        payload?: { error?: { message?: string } };
      };
      setStatus(
        apiError.status === 401 || apiError.status === 403
          ? "permission"
          : "error",
      );
      setMessage(
        apiError.payload?.error?.message ??
          "조직 관계와 적용기간을 저장할 수 없습니다.",
      );
    }
  }

  function cancelEdit() {
    const current = selected;
    if (current) {
      selectOrganization(current);
      setMessage("마지막 선택 상태로 복원했습니다.");
    }
  }

  return (
    <section
      className="space-y-6"
      aria-labelledby="organization-management-title"
    >
      <header>
        <p className="text-sm text-slate-500">
          시스템 관리 &gt; 사용자·조직 관리
        </p>
        <h1
          id="organization-management-title"
          className="text-2xl font-semibold text-slate-900"
        >
          조직 관리
        </h1>
      </header>
      <form
        onSubmit={submitSearch}
        className="flex flex-wrap gap-3 rounded-2xl border bg-white p-4 shadow-sm"
      >
        <input
          aria-label="조직코드"
          className="rounded-lg border p-2"
          placeholder="조직코드"
          value={organizationCode}
          onChange={(event) => setOrganizationCode(event.target.value)}
        />
        <select
          aria-label="조직유형"
          className="rounded-lg border p-2"
          value={organizationType}
          onChange={(event) => setOrganizationType(event.target.value)}
        >
          <option value="">조직유형 전체</option>
          {organizationTypes.map((type) => (
            <option key={type} value={type}>
              {type}
            </option>
          ))}
        </select>
        <button
          type="submit"
          className="rounded-lg bg-indigo-600 px-4 py-2 text-white"
        >
          조회
        </button>
        <button
          type="button"
          onClick={() => void refreshTree()}
          className="rounded-lg border px-4 py-2"
        >
          계층 새로고침
        </button>
      </form>
      <div role="status" className="rounded-xl bg-slate-50 p-3 text-sm">
        {message}
      </div>
      {status === "permission" && (
        <div className="rounded-xl border border-rose-200 bg-rose-50 p-4 text-rose-700">
          조직 관리 권한이 없습니다
        </div>
      )}
      <div className="grid gap-4 xl:grid-cols-3">
        <div className="rounded-2xl border bg-white p-4 shadow-sm">
          <h2 className="font-semibold">조직 목록</h2>
          <table className="mt-3 min-w-full text-sm">
            <thead>
              <tr>
                <th>조직코드</th>
                <th>조직명</th>
                <th>조직유형</th>
                <th>사용</th>
              </tr>
            </thead>
            <tbody>
              {status === "loading" && (
                <tr>
                  <td colSpan={4}>loading...</td>
                </tr>
              )}
              {status === "empty" && (
                <tr>
                  <td colSpan={4}>조건에 맞는 조직 없음</td>
                </tr>
              )}
              {organizations.map((org) => (
                <tr
                  key={org.organizationId}
                  onClick={() => selectOrganization(org)}
                  className="cursor-pointer border-t"
                >
                  <td>{org.organizationCode}</td>
                  <td>{org.organizationName}</td>
                  <td>{org.organizationType}</td>
                  <td>{org.isUsed ? "Y" : "N"}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        <div className="rounded-2xl border bg-white p-4 shadow-sm">
          <h2 className="font-semibold">조직 계층</h2>
          {tree.length === 0 ? (
            <p>등록된 상하위 관계 없음</p>
          ) : (
            <TreeList nodes={tree} onSelect={selectOrganization} />
          )}
        </div>
        <div className="rounded-2xl border bg-white p-4 shadow-sm">
          <h2 className="font-semibold">관계 및 적용기간 편집</h2>
          {!selected && <p>조직을 선택하세요</p>}
          {selected && (
            <div className="space-y-3">
              <p>
                조직코드: <input readOnly value={selected.organizationCode} />
              </p>
              <p>
                조직명: <input readOnly value={selected.organizationName} />
              </p>
              <p>
                조직유형: <input readOnly value={selected.organizationType} />
              </p>
              <label className="block">
                상위조직
                <select
                  className="mt-1 w-full rounded-lg border p-2"
                  value={parentId}
                  onChange={(event) => setParentId(event.target.value)}
                >
                  <option value="">상위조직 없음</option>
                  {organizations
                    .filter(
                      (org) => org.organizationId !== selected.organizationId,
                    )
                    .map((org) => (
                      <option
                        key={org.organizationId}
                        value={org.organizationId}
                      >
                        {org.organizationCode} {org.organizationName}
                      </option>
                    ))}
                </select>
              </label>
              <label className="block">
                적용 시작일
                <input
                  type="date"
                  className="mt-1 w-full rounded-lg border p-2"
                  value={effectiveStartDate}
                  onChange={(event) =>
                    setEffectiveStartDate(event.target.value)
                  }
                />
              </label>
              <label className="block">
                적용 종료일
                <input
                  type="date"
                  className="mt-1 w-full rounded-lg border p-2"
                  value={effectiveEndDate}
                  onChange={(event) => setEffectiveEndDate(event.target.value)}
                />
              </label>
              <label className="block">
                변경 사유
                <input
                  className="mt-1 w-full rounded-lg border p-2"
                  value={changeReason}
                  onChange={(event) => setChangeReason(event.target.value)}
                />
              </label>
              <div className="flex gap-2">
                <button
                  type="button"
                  onClick={saveRelationship}
                  className="rounded-lg bg-indigo-600 px-4 py-2 text-white"
                >
                  저장
                </button>
                <button
                  type="button"
                  onClick={cancelEdit}
                  className="rounded-lg border px-4 py-2"
                >
                  취소
                </button>
              </div>
            </div>
          )}
        </div>
      </div>
    </section>
  );
}
